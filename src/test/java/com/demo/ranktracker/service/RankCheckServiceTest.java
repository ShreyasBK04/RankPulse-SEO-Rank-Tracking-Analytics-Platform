package com.demo.ranktracker.service;

import com.demo.ranktracker.client.SearchApiClient;
import com.demo.ranktracker.entity.*;
import com.demo.ranktracker.repository.AlertRepository;
import com.demo.ranktracker.repository.KeywordRepository;
import com.demo.ranktracker.repository.RankingRepository;
import com.demo.ranktracker.repository.WebsiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankCheckServiceTest {

    @Mock
    private SearchApiClient searchApiClient;
    @Mock
    private KeywordRepository keywordRepository;
    @Mock
    private RankingRepository rankingRepository;
    @Mock
    private AlertRepository alertRepository;
    @Mock
    private WebsiteRepository websiteRepository;

    @InjectMocks
    private RankCheckService rankCheckService;

    private Website website;
    private Keyword keyword;

    @BeforeEach
    void setUp() {
        website = new Website("embarkx.com", "EmbarkX");
        website.setId(1L);

        keyword = new Keyword(website, "spring boot tutorial");
        keyword.setId(5L);
    }

    @Test
    void checkKeyword_shouldSkipIfAlreadyCheckedTodayWhenNotForced() {
        when(keywordRepository.findById(5L)).thenReturn(Optional.of(keyword));
        when(rankingRepository.existsByKeywordIdAndCheckedOn(eq(5L), any(LocalDate.class))).thenReturn(true);

        rankCheckService.checkKeyword(5L, false); // force = false

        verify(searchApiClient, never()).top100(anyString());
        verify(rankingRepository, never()).save(any());
    }

    @Test
    void checkKeyword_shouldMatchDomainAndSavePosition() {
        when(keywordRepository.findById(5L)).thenReturn(Optional.of(keyword));
        when(rankingRepository.findByKeywordIdAndCheckedOn(eq(5L), any(LocalDate.class))).thenReturn(Optional.empty());

        List<SearchApiClient.Result> results = List.of(
                new SearchApiClient.Result(1, "https://other.com/1", "other.com", "Other", "snippet"),
                new SearchApiClient.Result(2, "https://embarkx.com/tutorial", "embarkx.com", "Tutorial", "snippet")
        );
        when(searchApiClient.top100("spring boot tutorial")).thenReturn(results);

        rankCheckService.checkKeyword(5L, true);

        verify(rankingRepository).save(argThat(r ->
                r.getPosition() != null && r.getPosition() == 2 && r.getSearchUrl().equals("https://embarkx.com/tutorial")
        ));
    }

    @Test
    void checkKeyword_shouldSetNullPositionWhenDomainNotInTop100() {
        when(keywordRepository.findById(5L)).thenReturn(Optional.of(keyword));
        when(rankingRepository.findByKeywordIdAndCheckedOn(eq(5L), any(LocalDate.class))).thenReturn(Optional.empty());

        List<SearchApiClient.Result> results = List.of(
                new SearchApiClient.Result(1, "https://other.com/1", "other.com", "Other", "snippet")
        );
        when(searchApiClient.top100("spring boot tutorial")).thenReturn(results);

        rankCheckService.checkKeyword(5L, true);

        verify(rankingRepository).save(argThat(r -> r.getPosition() == null));
    }
}
