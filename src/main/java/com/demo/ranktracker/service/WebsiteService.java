package com.demo.ranktracker.service;

import com.demo.ranktracker.dto.WebsiteCreateRequest;
import com.demo.ranktracker.dto.WebsiteDTO;
import com.demo.ranktracker.entity.Website;
import com.demo.ranktracker.exception.DuplicateResourceException;
import com.demo.ranktracker.exception.ResourceNotFoundException;
import com.demo.ranktracker.repository.KeywordRepository;
import com.demo.ranktracker.repository.WebsiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WebsiteService {

    private final WebsiteRepository websiteRepository;
    private final KeywordRepository keywordRepository;

    public WebsiteService(WebsiteRepository websiteRepository,
                          KeywordRepository keywordRepository) {
        this.websiteRepository = websiteRepository;
        this.keywordRepository = keywordRepository;
    }

    public List<WebsiteDTO> getAllWebsites() {
        return websiteRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public WebsiteDTO getWebsite(Long id) {
        Website website = websiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Website", id));
        return toDTO(website);
    }

    @Transactional
    public WebsiteDTO createWebsite(WebsiteCreateRequest request) {
        String domain = cleanDomain(request.domain());
        if (websiteRepository.existsByDomain(domain)) {
            throw new DuplicateResourceException("Website with domain '" + domain + "' already exists");
        }

        Website website = new Website();
        website.setDomain(domain);
        website.setName(request.name() != null && !request.name().isBlank() ? request.name().trim() : domain);

        Website saved = websiteRepository.save(website);
        return toDTO(saved);
    }

    @Transactional
    public WebsiteDTO updateWebsite(Long id, WebsiteCreateRequest request) {
        Website website = websiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Website", id));

        String domain = cleanDomain(request.domain());
        websiteRepository.findByDomain(domain)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Website with domain '" + domain + "' already exists");
                });

        website.setDomain(domain);
        website.setName(request.name() != null && !request.name().isBlank() ? request.name().trim() : domain);

        Website saved = websiteRepository.save(website);
        return toDTO(saved);
    }

    public static String cleanDomain(String input) {
        if (input == null) return "";
        String s = input.trim().toLowerCase();
        s = s.replaceFirst("^https?://", "");
        s = s.split("/")[0];
        s = s.split("\\?")[0];
        s = s.split(":")[0];
        s = s.replaceFirst("^www\\.", "");
        return s;
    }

    @Transactional
    public void deleteWebsite(Long id) {
        if (!websiteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Website", id);
        }
        websiteRepository.deleteById(id);
    }

    private WebsiteDTO toDTO(Website website) {
        long keywordCount = keywordRepository.countByWebsiteId(website.getId());
        return new WebsiteDTO(
                website.getId(),
                website.getDomain(),
                website.getName(),
                keywordCount,
                website.getCreatedAt(),
                website.getUpdatedAt()
        );
    }
}
