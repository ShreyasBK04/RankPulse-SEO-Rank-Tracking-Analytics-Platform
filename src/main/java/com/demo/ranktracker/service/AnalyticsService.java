package com.demo.ranktracker.service;

import com.demo.ranktracker.dto.*;
import com.demo.ranktracker.entity.Keyword;
import com.demo.ranktracker.entity.Ranking;
import com.demo.ranktracker.exception.ResourceNotFoundException;
import com.demo.ranktracker.repository.KeywordRepository;
import com.demo.ranktracker.repository.RankingRepository;
import com.demo.ranktracker.repository.WebsiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final WebsiteRepository websiteRepository;
    private final KeywordRepository keywordRepository;
    private final RankingRepository rankingRepository;

    public AnalyticsService(WebsiteRepository websiteRepository,
                            KeywordRepository keywordRepository,
                            RankingRepository rankingRepository) {
        this.websiteRepository = websiteRepository;
        this.keywordRepository = keywordRepository;
        this.rankingRepository = rankingRepository;
    }

    /**
     * Dashboard summary statistics for a website.
     */
    public DashboardStatsDTO getDashboardStats(Long websiteId) {
        validateWebsite(websiteId);
        List<Keyword> keywords = keywordRepository.findByWebsiteId(websiteId);
        long totalKeywords = keywords.size();

        if (totalKeywords == 0) {
            return new DashboardStatsDTO(0, null, 0, 0, 0, 0, null);
        }

        int top10 = 0, top100 = 0, improved = 0, declined = 0;
        double positionSum = 0;
        int rankedCount = 0;
        LocalDateTime lastChecked = null;

        for (Keyword kw : keywords) {
            List<Ranking> recent = rankingRepository.findTop2ByKeywordIdOrderByCheckedOnDesc(kw.getId());
            if (recent.isEmpty()) continue;

            Ranking latest = recent.getFirst();
            if (latest.getPosition() != null) {
                rankedCount++;
                positionSum += latest.getPosition();
                if (latest.getPosition() <= 10) top10++;
                if (latest.getPosition() <= 100) top100++;
            }
            if (lastChecked == null || latest.getCreatedAt().isAfter(lastChecked)) {
                lastChecked = latest.getCreatedAt();
            }

            // Compare current vs previous to determine improvement/decline
            if (recent.size() > 1) {
                Ranking previous = recent.get(1);
                if (latest.getPosition() != null && previous.getPosition() != null) {
                    // Lower position number = better ranking
                    if (latest.getPosition() < previous.getPosition()) improved++;
                    if (latest.getPosition() > previous.getPosition()) declined++;
                } else if (latest.getPosition() != null && previous.getPosition() == null) {
                    improved++; // appeared in top 100
                } else if (latest.getPosition() == null && previous.getPosition() != null) {
                    declined++; // dropped out of top 100
                }
            }
        }

        Double avgPosition = rankedCount > 0 ? Math.round(positionSum / rankedCount * 10.0) / 10.0 : null;

        return new DashboardStatsDTO(
                totalKeywords, avgPosition, top10, top100, improved, declined, lastChecked
        );
    }

    /**
     * Keyword performance table for the dashboard.
     */
    public List<KeywordPerformanceDTO> getKeywordPerformance(Long websiteId) {
        validateWebsite(websiteId);
        List<Keyword> keywords = keywordRepository.findByWebsiteId(websiteId);

        return keywords.stream().map(kw -> {
            List<Ranking> recent = rankingRepository.findTop2ByKeywordIdOrderByCheckedOnDesc(kw.getId());
            Integer bestPosition = rankingRepository.findBestPosition(kw.getId()).orElse(null);

            Integer current = null, previous = null;
            String searchUrl = null;
            LocalDate lastChecked = null;

            if (!recent.isEmpty()) {
                current = recent.getFirst().getPosition();
                searchUrl = recent.getFirst().getSearchUrl();
                lastChecked = recent.getFirst().getCheckedOn();
            }
            if (recent.size() > 1) {
                previous = recent.get(1).getPosition();
            }

            Integer change = computeChange(current, previous);
            String trend = computeTrend(current, previous);

            return new KeywordPerformanceDTO(
                    kw.getId(), kw.getKeyword(), current, previous,
                    change, bestPosition, searchUrl, lastChecked, trend
            );
        }).toList();
    }

    /**
     * Ranking trends over time for charts.
     */
    public TrendDataDTO getRankingTrends(Long websiteId, LocalDate startDate, LocalDate endDate) {
        validateWebsite(websiteId);
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        List<Keyword> keywords = keywordRepository.findByWebsiteId(websiteId);
        List<Ranking> allRankings = rankingRepository.findByWebsiteIdAndDateRange(websiteId, start, end);

        // Group rankings by date for average position trend
        Map<LocalDate, List<Ranking>> byDate = allRankings.stream()
                .collect(Collectors.groupingBy(Ranking::getCheckedOn, TreeMap::new, Collectors.toList()));

        List<TrendDataDTO.DataPoint> dataPoints = byDate.entrySet().stream()
                .map(entry -> {
                    List<Ranking> dayRankings = entry.getValue();
                    double avg = dayRankings.stream()
                            .filter(r -> r.getPosition() != null)
                            .mapToInt(Ranking::getPosition)
                            .average()
                            .orElse(0);
                    return new TrendDataDTO.DataPoint(
                            entry.getKey(),
                            avg > 0 ? Math.round(avg * 10.0) / 10.0 : null,
                            dayRankings.size()
                    );
                }).toList();

        // Per-keyword lines
        List<TrendDataDTO.KeywordLine> keywordLines = keywords.stream().map(kw -> {
            List<Ranking> kwRankings = allRankings.stream()
                    .filter(r -> r.getKeyword().getId().equals(kw.getId()))
                    .sorted(Comparator.comparing(Ranking::getCheckedOn))
                    .toList();

            List<TrendDataDTO.PositionPoint> positions = kwRankings.stream()
                    .map(r -> new TrendDataDTO.PositionPoint(r.getCheckedOn(), r.getPosition()))
                    .toList();

            return new TrendDataDTO.KeywordLine(kw.getId(), kw.getKeyword(), positions);
        }).filter(line -> !line.positions().isEmpty()).toList();

        return new TrendDataDTO(dataPoints, keywordLines);
    }

    /**
     * Ranking distribution — how many keywords fall in each range.
     */
    public DistributionDTO getRankingDistribution(Long websiteId) {
        validateWebsite(websiteId);
        List<Keyword> keywords = keywordRepository.findByWebsiteId(websiteId);

        long top3 = 0, top10 = 0, top20 = 0, top50 = 0, top100 = 0, notRanking = 0;

        for (Keyword kw : keywords) {
            Optional<Ranking> latest = rankingRepository.findTopByKeywordIdOrderByCheckedOnDesc(kw.getId());
            if (latest.isEmpty() || latest.get().getPosition() == null) {
                notRanking++;
            } else {
                int pos = latest.get().getPosition();
                if (pos <= 3) top3++;
                else if (pos <= 10) top10++;
                else if (pos <= 20) top20++;
                else if (pos <= 50) top50++;
                else top100++;
            }
        }

        return new DistributionDTO(top3, top10, top20, top50, top100, notRanking);
    }

    /**
     * Compute ranking change. Positive = improved (moved up), negative = declined (moved down).
     * Remember: lower position number = better ranking.
     */
    private Integer computeChange(Integer current, Integer previous) {
        if (current == null || previous == null) return null;
        return previous - current; // positive means improvement
    }

    private String computeTrend(Integer current, Integer previous) {
        if (current == null && previous == null) return "new";
        if (current == null) return "down";
        if (previous == null) return "up";
        if (current < previous) return "up";
        if (current > previous) return "down";
        return "stable";
    }

    private void validateWebsite(Long websiteId) {
        if (!websiteRepository.existsById(websiteId)) {
            throw new ResourceNotFoundException("Website", websiteId);
        }
    }
}
