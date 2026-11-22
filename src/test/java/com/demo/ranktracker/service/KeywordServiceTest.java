package com.demo.ranktracker.service;

import com.demo.ranktracker.dto.KeywordCreateRequest;
import com.demo.ranktracker.dto.KeywordDTO;
import com.demo.ranktracker.entity.Keyword;
import com.demo.ranktracker.entity.Website;
import com.demo.ranktracker.exception.DuplicateResourceException;
import com.demo.ranktracker.exception.ResourceNotFoundException;
import com.demo.ranktracker.repository.KeywordRepository;
import com.demo.ranktracker.repository.RankingRepository;
import com.demo.ranktracker.repository.WebsiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeywordServiceTest {

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private WebsiteRepository websiteRepository;

    @Mock
    private RankingRepository rankingRepository;

    @InjectMocks
    private KeywordService keywordService;

    private Website website;
    private Keyword keyword;

    @BeforeEach
    void setUp() {
        website = new Website("embarkx.com", "EmbarkX");
        website.setId(1L);

        keyword = new Keyword(website, "spring boot tutorial");
        keyword.setId(10L);
    }

    @Test
    void createKeyword_shouldSaveWhenUnique() {
        KeywordCreateRequest req = new KeywordCreateRequest("spring boot tutorial");
        when(websiteRepository.findById(1L)).thenReturn(Optional.of(website));
        when(keywordRepository.existsByWebsiteIdAndKeyword(1L, "spring boot tutorial")).thenReturn(false);
        when(keywordRepository.save(any(Keyword.class))).thenAnswer(inv -> {
            Keyword k = inv.getArgument(0);
            k.setId(11L);
            return k;
        });

        KeywordDTO result = keywordService.createKeyword(1L, req);

        assertNotNull(result);
        assertEquals("spring boot tutorial", result.keyword());
        verify(keywordRepository).save(any(Keyword.class));
    }

    @Test
    void createKeyword_shouldThrowDuplicateWhenAlreadyExists() {
        KeywordCreateRequest req = new KeywordCreateRequest("spring boot tutorial");
        when(websiteRepository.findById(1L)).thenReturn(Optional.of(website));
        when(keywordRepository.existsByWebsiteIdAndKeyword(1L, "spring boot tutorial")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> keywordService.createKeyword(1L, req));
        verify(keywordRepository, never()).save(any());
    }

    @Test
    void deleteKeyword_shouldDeleteWhenExists() {
        when(keywordRepository.existsById(10L)).thenReturn(true);

        keywordService.deleteKeyword(10L);

        verify(keywordRepository).deleteById(10L);
    }
}
