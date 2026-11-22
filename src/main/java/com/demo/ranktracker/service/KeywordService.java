package com.demo.ranktracker.service;

import com.demo.ranktracker.dto.KeywordCreateRequest;
import com.demo.ranktracker.dto.KeywordDTO;
import com.demo.ranktracker.entity.Keyword;
import com.demo.ranktracker.entity.Ranking;
import com.demo.ranktracker.entity.Website;
import com.demo.ranktracker.exception.DuplicateResourceException;
import com.demo.ranktracker.exception.ResourceNotFoundException;
import com.demo.ranktracker.repository.KeywordRepository;
import com.demo.ranktracker.repository.RankingRepository;
import com.demo.ranktracker.repository.WebsiteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final WebsiteRepository websiteRepository;
    private final RankingRepository rankingRepository;

    public KeywordService(KeywordRepository keywordRepository,
                          WebsiteRepository websiteRepository,
                          RankingRepository rankingRepository) {
        this.keywordRepository = keywordRepository;
        this.websiteRepository = websiteRepository;
        this.rankingRepository = rankingRepository;
    }

    public List<KeywordDTO> getKeywordsByWebsite(Long websiteId) {
        validateWebsiteExists(websiteId);
        return keywordRepository.findByWebsiteId(websiteId).stream()
                .map(this::toDTO)
                .toList();
    }

    public Page<KeywordDTO> getKeywordsByWebsite(Long websiteId, String search, Pageable pageable) {
        validateWebsiteExists(websiteId);
        if (search != null && !search.isBlank()) {
            return keywordRepository
                    .findByWebsiteIdAndKeywordContainingIgnoreCase(websiteId, search.trim(), pageable)
                    .map(this::toDTO);
        }
        return keywordRepository.findByWebsiteId(websiteId, pageable)
                .map(this::toDTO);
    }

    public KeywordDTO getKeyword(Long id) {
        Keyword keyword = keywordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Keyword", id));
        return toDTO(keyword);
    }

    @Transactional
    public KeywordDTO createKeyword(Long websiteId, KeywordCreateRequest request) {
        Website website = websiteRepository.findById(websiteId)
                .orElseThrow(() -> new ResourceNotFoundException("Website", websiteId));

        String keywordText = request.keyword().trim().toLowerCase();
        if (keywordRepository.existsByWebsiteIdAndKeyword(websiteId, keywordText)) {
            throw new DuplicateResourceException(
                    "Keyword '" + keywordText + "' already exists for this website");
        }

        Keyword keyword = new Keyword(website, keywordText);
        Keyword saved = keywordRepository.save(keyword);
        return toDTO(saved);
    }

    @Transactional
    public void deleteKeyword(Long id) {
        if (!keywordRepository.existsById(id)) {
            throw new ResourceNotFoundException("Keyword", id);
        }
        keywordRepository.deleteById(id);
    }

    private void validateWebsiteExists(Long websiteId) {
        if (!websiteRepository.existsById(websiteId)) {
            throw new ResourceNotFoundException("Website", websiteId);
        }
    }

    private KeywordDTO toDTO(Keyword keyword) {
        List<Ranking> recent = rankingRepository.findTop2ByKeywordIdOrderByCheckedOnDesc(keyword.getId());
        Integer bestPosition = rankingRepository.findBestPosition(keyword.getId()).orElse(null);

        Integer current = null;
        Integer change = null;
        String searchUrl = null;
        LocalDate lastChecked = null;

        if (!recent.isEmpty()) {
            current = recent.getFirst().getPosition();
            searchUrl = recent.getFirst().getSearchUrl();
            lastChecked = recent.getFirst().getCheckedOn();

            if (recent.size() > 1 && recent.get(1).getPosition() != null && current != null) {
                change = recent.get(1).getPosition() - current;
            }
        }

        return new KeywordDTO(
                keyword.getId(),
                keyword.getWebsite().getId(),
                keyword.getWebsite().getDomain(),
                keyword.getKeyword(),
                current,
                change,
                bestPosition,
                searchUrl,
                lastChecked,
                keyword.getCreatedAt()
        );
    }
}
