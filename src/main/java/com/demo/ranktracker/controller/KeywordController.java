package com.demo.ranktracker.controller;

import com.demo.ranktracker.dto.KeywordCreateRequest;
import com.demo.ranktracker.dto.KeywordDTO;
import com.demo.ranktracker.service.KeywordService;
import com.demo.ranktracker.service.RankCheckService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class KeywordController {

    private static final Logger log = LoggerFactory.getLogger(KeywordController.class);

    private final KeywordService keywordService;
    private final RankCheckService rankCheckService;

    public KeywordController(KeywordService keywordService,
                             RankCheckService rankCheckService) {
        this.keywordService = keywordService;
        this.rankCheckService = rankCheckService;
    }

    @GetMapping("/websites/{websiteId}/keywords")
    public ResponseEntity<Page<KeywordDTO>> getKeywords(
            @PathVariable Long websiteId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(keywordService.getKeywordsByWebsite(websiteId, search, pageRequest));
    }

    @PostMapping("/websites/{websiteId}/keywords")
    public ResponseEntity<KeywordDTO> createKeyword(
            @PathVariable Long websiteId,
            @Valid @RequestBody KeywordCreateRequest request) {
        KeywordDTO created = keywordService.createKeyword(websiteId, request);

        // Check ranking immediately so user sees real rank right away
        try {
            rankCheckService.checkKeyword(created.id(), true);
            // Return updated DTO with the freshly discovered rank
            return ResponseEntity.status(HttpStatus.CREATED).body(keywordService.getKeyword(created.id()));
        } catch (Exception e) {
            log.warn("Initial rank check for '{}' failed: {}", created.keyword(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
    }

    @GetMapping("/keywords/{id}")
    public ResponseEntity<KeywordDTO> getKeyword(@PathVariable Long id) {
        return ResponseEntity.ok(keywordService.getKeyword(id));
    }

    @DeleteMapping("/keywords/{id}")
    public ResponseEntity<Void> deleteKeyword(@PathVariable Long id) {
        keywordService.deleteKeyword(id);
        return ResponseEntity.noContent().build();
    }
}
