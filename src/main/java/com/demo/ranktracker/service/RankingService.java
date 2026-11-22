package com.demo.ranktracker.service;

import com.demo.ranktracker.dto.RankingDTO;
import com.demo.ranktracker.dto.RankingHistoryDTO;
import com.demo.ranktracker.entity.Keyword;
import com.demo.ranktracker.entity.Ranking;
import com.demo.ranktracker.exception.ResourceNotFoundException;
import com.demo.ranktracker.repository.KeywordRepository;
import com.demo.ranktracker.repository.RankingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RankingService {

    private final RankingRepository rankingRepository;
    private final KeywordRepository keywordRepository;

    public RankingService(RankingRepository rankingRepository,
                          KeywordRepository keywordRepository) {
        this.rankingRepository = rankingRepository;
        this.keywordRepository = keywordRepository;
    }

    public RankingHistoryDTO getKeywordHistory(Long keywordId, LocalDate startDate, LocalDate endDate) {
        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new ResourceNotFoundException("Keyword", keywordId));

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        List<Ranking> rankings = rankingRepository
                .findByKeywordIdAndCheckedOnBetweenOrderByCheckedOn(keywordId, start, end);

        Integer bestPosition = rankingRepository.findBestPosition(keywordId).orElse(null);
        Integer currentPosition = rankingRepository.findTopByKeywordIdOrderByCheckedOnDesc(keywordId)
                .map(Ranking::getPosition)
                .orElse(null);

        List<RankingHistoryDTO.DataPoint> history = rankings.stream()
                .map(r -> new RankingHistoryDTO.DataPoint(r.getCheckedOn(), r.getPosition()))
                .toList();

        return new RankingHistoryDTO(
                keyword.getId(),
                keyword.getKeyword(),
                currentPosition,
                bestPosition,
                history
        );
    }

    public List<RankingDTO> getLatestRankings(Long keywordId) {
        if (!keywordRepository.existsById(keywordId)) {
            throw new ResourceNotFoundException("Keyword", keywordId);
        }
        return rankingRepository.findByKeywordIdOrderByCheckedOnDesc(keywordId).stream()
                .map(this::toDTO)
                .toList();
    }

    private RankingDTO toDTO(Ranking ranking) {
        return new RankingDTO(
                ranking.getId(),
                ranking.getKeyword().getId(),
                ranking.getKeyword().getKeyword(),
                ranking.getPosition(),
                ranking.getSearchUrl(),
                ranking.getCheckedOn(),
                ranking.getCreatedAt()
        );
    }
}
