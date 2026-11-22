package com.demo.ranktracker.service;

import com.demo.ranktracker.dto.WebsiteCreateRequest;
import com.demo.ranktracker.dto.WebsiteDTO;
import com.demo.ranktracker.entity.Website;
import com.demo.ranktracker.exception.DuplicateResourceException;
import com.demo.ranktracker.exception.ResourceNotFoundException;
import com.demo.ranktracker.repository.KeywordRepository;
import com.demo.ranktracker.repository.WebsiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebsiteServiceTest {

    @Mock
    private WebsiteRepository websiteRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @InjectMocks
    private WebsiteService websiteService;

    private Website website;

    @BeforeEach
    void setUp() {
        website = new Website("embarkx.com", "EmbarkX");
        website.setId(1L);
    }

    @Test
    void getAllWebsites_shouldReturnListOfWebsites() {
        when(websiteRepository.findAll()).thenReturn(List.of(website));
        when(keywordRepository.countByWebsiteId(1L)).thenReturn(5L);

        List<WebsiteDTO> result = websiteService.getAllWebsites();

        assertEquals(1, result.size());
        assertEquals("embarkx.com", result.get(0).domain());
        assertEquals(5L, result.get(0).keywordCount());
    }

    @Test
    void getWebsite_shouldReturnWebsiteWhenFound() {
        when(websiteRepository.findById(1L)).thenReturn(Optional.of(website));
        when(keywordRepository.countByWebsiteId(1L)).thenReturn(3L);

        WebsiteDTO result = websiteService.getWebsite(1L);

        assertNotNull(result);
        assertEquals("embarkx.com", result.domain());
    }

    @Test
    void getWebsite_shouldThrowNotFoundWhenNotFound() {
        when(websiteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> websiteService.getWebsite(99L));
    }

    @Test
    void createWebsite_shouldSaveWhenDomainIsUnique() {
        WebsiteCreateRequest req = new WebsiteCreateRequest("newsite.com", "New Site");
        when(websiteRepository.existsByDomain("newsite.com")).thenReturn(false);
        when(websiteRepository.save(any(Website.class))).thenAnswer(inv -> {
            Website w = inv.getArgument(0);
            w.setId(2L);
            return w;
        });

        WebsiteDTO created = websiteService.createWebsite(req);

        assertNotNull(created);
        assertEquals("newsite.com", created.domain());
        verify(websiteRepository).save(any(Website.class));
    }

    @Test
    void createWebsite_shouldThrowDuplicateWhenDomainExists() {
        WebsiteCreateRequest req = new WebsiteCreateRequest("embarkx.com", "EmbarkX");
        when(websiteRepository.existsByDomain("embarkx.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> websiteService.createWebsite(req));
        verify(websiteRepository, never()).save(any());
    }

    @Test
    void deleteWebsite_shouldDeleteWhenExists() {
        when(websiteRepository.existsById(1L)).thenReturn(true);

        websiteService.deleteWebsite(1L);

        verify(websiteRepository).deleteById(1L);
    }

    @Test
    void deleteWebsite_shouldThrowWhenNotExists() {
        when(websiteRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> websiteService.deleteWebsite(99L));
    }
}
