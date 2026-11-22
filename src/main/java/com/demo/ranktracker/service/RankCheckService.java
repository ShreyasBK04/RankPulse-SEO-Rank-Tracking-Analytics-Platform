package com.demo.ranktracker.service;

import com.demo.ranktracker.client.SearchApiClient;
import com.demo.ranktracker.entity.*;
import com.demo.ranktracker.exception.RankCheckException;
import com.demo.ranktracker.exception.ResourceNotFoundException;
import com.demo.ranktracker.repository.AlertRepository;
import com.demo.ranktracker.repository.KeywordRepository;
import com.demo.ranktracker.repository.RankingRepository;
import com.demo.ranktracker.repository.WebsiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Core rank-checking service.
 * Handles domain matching across subdomains, protocols, and URLs,
 * updates existing daily rankings or inserts new ones,
 * and triggers alerts for significant SERP movements.
 */
@Service
public class RankCheckService {

    private static final Logger log = LoggerFactory.getLogger(RankCheckService.class);

    private final SearchApiClient searchApiClient;
    private final KeywordRepository keywordRepository;
    private final RankingRepository rankingRepository;
    private final AlertRepository alertRepository;
    private final WebsiteRepository websiteRepository;

    public RankCheckService(SearchApiClient searchApiClient,
                            KeywordRepository keywordRepository,
                            RankingRepository rankingRepository,
                            AlertRepository alertRepository,
                            WebsiteRepository websiteRepository) {
        this.searchApiClient = searchApiClient;
        this.keywordRepository = keywordRepository;
        this.rankingRepository = rankingRepository;
        this.alertRepository = alertRepository;
        this.websiteRepository = websiteRepository;
    }

    /**
     * Check ranking for a single keyword with force option.
     * @param keywordId ID of the keyword
     * @param force if true, ignores already-checked-today restriction and updates today's record
     */
    @Transactional
    public void checkKeyword(Long keywordId, boolean force) {
        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new ResourceNotFoundException("Keyword", keywordId));

        String domain = keyword.getWebsite().getDomain();
        LocalDate today = LocalDate.now();

        // If not forced and already checked today, skip to conserve credits
        if (!force && rankingRepository.existsByKeywordIdAndCheckedOn(keywordId, today)) {
            log.info("Already checked keyword '{}' today (force=false). Skipping check.", keyword.getKeyword());
            return;
        }

        try {
            // Find previous position before this check to compare for alerts
            Integer oldPosition = findPreviousPosition(keywordId, today);

            // Call SearchAPI
            List<SearchApiClient.Result> results = searchApiClient.top100(keyword.getKeyword());

            // Robust domain matching
            SearchApiClient.Result match = results.stream()
                    .filter(r -> isDomainMatch(domain, r))
                    .findFirst()
                    .orElse(null);

            Integer newPosition = match != null ? match.position() : null;
            String searchUrl = match != null ? match.link() : null;

            // If a ranking already exists for today, UPDATE it; otherwise insert a new record
            Optional<Ranking> existingToday = rankingRepository.findByKeywordIdAndCheckedOn(keywordId, today);
            Ranking ranking = existingToday.orElseGet(() -> {
                Ranking r = new Ranking();
                r.setKeyword(keyword);
                r.setCheckedOn(today);
                return r;
            });

            ranking.setPosition(newPosition);
            ranking.setSearchUrl(searchUrl);
            rankingRepository.save(ranking);

            // Generate alerts based on old position vs new position
            generateAlerts(keyword, oldPosition, newPosition);

            log.info("Checked '{}' for domain '{}': {}", keyword.getKeyword(), domain,
                    newPosition == null ? "Not in top 100" : "#" + newPosition + " (" + searchUrl + ")");

        } catch (Exception e) {
            log.error("Failed to check keyword '{}': {}", keyword.getKeyword(), e.getMessage());
            throw new RankCheckException("Failed to check keyword '" + keyword.getKeyword() + "': " + e.getMessage(), e);
        }
    }

    public void checkKeyword(Long keywordId) {
        checkKeyword(keywordId, true);
    }

    /**
     * Check all keywords for a website.
     * @param websiteId ID of website
     * @param force if true, forces fresh check even if checked today
     */
    public int checkWebsite(Long websiteId, boolean force) {
        if (!websiteRepository.existsById(websiteId)) {
            throw new ResourceNotFoundException("Website", websiteId);
        }

        List<Keyword> keywords = keywordRepository.findByWebsiteId(websiteId);
        int checked = 0;

        for (Keyword keyword : keywords) {
            try {
                checkKeyword(keyword.getId(), force);
                checked++;
            } catch (Exception e) {
                log.error("Error checking keyword '{}': {}", keyword.getKeyword(), e.getMessage());
            }
        }

        log.info("Rank check complete: checked {}/{} keywords for website ID {}", checked, keywords.size(), websiteId);
        return checked;
    }

    public int checkWebsite(Long websiteId) {
        return checkWebsite(websiteId, true);
    }

    /**
     * Scheduled daily check for all keywords across all websites.
     */
    @Scheduled(cron = "${tracker.check-cron}")
    public void scheduledCheckAll() {
        log.info("Executing scheduled rank check for all keywords");
        List<Keyword> allKeywords = keywordRepository.findAll();
        int checked = 0;

        for (Keyword keyword : allKeywords) {
            try {
                // Scheduled check does NOT force: skips if already checked today
                checkKeyword(keyword.getId(), false);
                checked++;
            } catch (Exception e) {
                log.error("Scheduled check error for keyword '{}': {}", keyword.getKeyword(), e.getMessage());
            }
        }

        log.info("Scheduled rank check complete: {}/{} processed", checked, allKeywords.size());
    }

    /**
     * Robust domain matching:
     * - Strips protocols (http/https), www, paths, queries
     * - Matches exact domains, subdomains (docs.spring.io matches spring.io), and URL links
     */
    private boolean isDomainMatch(String targetDomain, SearchApiClient.Result result) {
        if (targetDomain == null || targetDomain.isBlank() || result == null) return false;

        String cleanTarget = normalizeHost(targetDomain);

        // 1. Check result.domain()
        if (result.domain() != null && !result.domain().isBlank()) {
            String cleanResultDomain = normalizeHost(result.domain());
            if (matches(cleanTarget, cleanResultDomain)) {
                return true;
            }
        }

        // 2. Fallback: parse host from result.link()
        if (result.link() != null && !result.link().isBlank()) {
            try {
                URI uri = URI.create(result.link().trim());
                String host = uri.getHost();
                if (host != null) {
                    String cleanHost = normalizeHost(host);
                    if (matches(cleanTarget, cleanHost)) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
                // Fallback string matching
                String linkLower = result.link().toLowerCase();
                if (linkLower.contains("://" + cleanTarget) || linkLower.contains("." + cleanTarget + "/")) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matches(String target, String candidate) {
        if (target.equalsIgnoreCase(candidate)) return true;
        // Subdomain of target: e.g. docs.spring.io ends with .spring.io
        if (candidate.toLowerCase().endsWith("." + target.toLowerCase())) return true;
        // Target is subdomain of candidate: e.g. target was www.spring.io, candidate is spring.io
        if (target.toLowerCase().endsWith("." + candidate.toLowerCase())) return true;
        return false;
    }

    private String normalizeHost(String domainOrUrl) {
        if (domainOrUrl == null) return "";
        String s = domainOrUrl.trim().toLowerCase();
        s = s.replaceFirst("^https?://", "");
        s = s.split("/")[0]; // remove path if any
        s = s.split("\\?")[0]; // remove query if any
        s = s.split(":")[0];  // remove port if any
        s = s.replaceFirst("^www\\.", "");
        return s;
    }

    private Integer findPreviousPosition(Long keywordId, LocalDate today) {
        List<Ranking> recent = rankingRepository.findTop2ByKeywordIdOrderByCheckedOnDesc(keywordId);
        if (recent.isEmpty()) return null;

        // If the latest ranking is from today, the previous position is the 2nd latest (if exists)
        if (recent.getFirst().getCheckedOn().equals(today)) {
            return recent.size() > 1 ? recent.get(1).getPosition() : null;
        }

        // Otherwise, the latest ranking is the previous position
        return recent.getFirst().getPosition();
    }

    private void generateAlerts(Keyword keyword, Integer oldPosition, Integer newPosition) {
        if (Objects.equals(oldPosition, newPosition)) return;

        // Dropped out of top 100
        if (oldPosition != null && newPosition == null) {
            createAlert(keyword, AlertType.DROPPED_FROM_TOP_100, oldPosition, null,
                    "'" + keyword.getKeyword() + "' dropped out of the top 100 (was #" + oldPosition + ")");
            return;
        }

        // Entered top 3
        if (newPosition != null && newPosition <= 3 && (oldPosition == null || oldPosition > 3)) {
            createAlert(keyword, AlertType.ENTERED_TOP_3, oldPosition, newPosition,
                    "'" + keyword.getKeyword() + "' entered the top 3 (#"
                    + (oldPosition == null ? "unranked" : oldPosition) + " → #" + newPosition + ")");
        }
        // Entered top 10
        else if (newPosition != null && newPosition <= 10 && (oldPosition == null || oldPosition > 10)) {
            createAlert(keyword, AlertType.ENTERED_TOP_10, oldPosition, newPosition,
                    "'" + keyword.getKeyword() + "' entered the top 10 (#"
                    + (oldPosition == null ? "unranked" : oldPosition) + " → #" + newPosition + ")");
        }
        // Significant improvement (5+ positions)
        else if (oldPosition != null && newPosition != null && oldPosition - newPosition >= 5) {
            createAlert(keyword, AlertType.SIGNIFICANT_IMPROVEMENT, oldPosition, newPosition,
                    "'" + keyword.getKeyword() + "' improved " + (oldPosition - newPosition)
                    + " positions (#" + oldPosition + " → #" + newPosition + ")");
        }
        // Significant decline (5+ positions)
        else if (oldPosition != null && newPosition != null && newPosition - oldPosition >= 5) {
            createAlert(keyword, AlertType.SIGNIFICANT_DECLINE, oldPosition, newPosition,
                    "'" + keyword.getKeyword() + "' declined " + (newPosition - oldPosition)
                    + " positions (#" + oldPosition + " → #" + newPosition + ")");
        }
    }

    private void createAlert(Keyword keyword, AlertType type,
                             Integer oldPosition, Integer newPosition, String message) {
        Alert alert = new Alert(keyword, type, message, oldPosition, newPosition);
        alertRepository.save(alert);
        log.info("SERP Alert created: {}", message);
    }
}
