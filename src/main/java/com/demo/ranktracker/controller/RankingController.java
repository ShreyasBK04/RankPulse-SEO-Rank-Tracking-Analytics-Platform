package com.demo.ranktracker.controller;

import com.demo.ranktracker.dto.KeywordDTO;
import com.demo.ranktracker.dto.RankingDTO;
import com.demo.ranktracker.dto.RankingHistoryDTO;
import com.demo.ranktracker.service.KeywordService;
import com.demo.ranktracker.service.RankCheckService;
import com.demo.ranktracker.service.RankingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RankingController {

    private final RankCheckService rankCheckService;
    private final RankingService rankingService;
    private final KeywordService keywordService;

    public RankingController(RankCheckService rankCheckService,
                             RankingService rankingService,
                             KeywordService keywordService) {
        this.rankCheckService = rankCheckService;
        this.rankingService = rankingService;
        this.keywordService = keywordService;
    }

    /**
     * Manually trigger a rank check for a single keyword.
     */
    @PostMapping("/rankings/check/{keywordId}")
    public ResponseEntity<Map<String, Object>> checkKeyword(@PathVariable Long keywordId) {
        rankCheckService.checkKeyword(keywordId);
        KeywordDTO updated = keywordService.getKeyword(keywordId);
        return ResponseEntity.ok(Map.of(
                "message", "Rank check completed successfully",
                "keywordId", keywordId,
                "position", updated.currentPosition() != null ? updated.currentPosition() : "Not in top 100",
                "searchUrl", updated.searchUrl() != null ? updated.searchUrl() : "",
                "change", updated.change() != null ? updated.change() : 0
        ));
    }

    /**
     * Manually trigger rank checks for all keywords of a website.
     */
    @PostMapping("/rankings/check/website/{websiteId}")
    public ResponseEntity<Map<String, Object>> checkWebsite(@PathVariable Long websiteId) {
        int checked = rankCheckService.checkWebsite(websiteId);
        return ResponseEntity.ok(Map.of(
                "message", "Rank check completed",
                "keywordsChecked", checked
        ));
    }

    /**
     * Get ranking history for a specific keyword.
     */
    @GetMapping("/keywords/{keywordId}/rankings")
    public ResponseEntity<RankingHistoryDTO> getKeywordHistory(
            @PathVariable Long keywordId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(rankingService.getKeywordHistory(keywordId, startDate, endDate));
    }

    /**
     * Get all rankings for a keyword (most recent first).
     */
    @GetMapping("/keywords/{keywordId}/rankings/all")
    public ResponseEntity<List<RankingDTO>> getAllRankings(@PathVariable Long keywordId) {
        return ResponseEntity.ok(rankingService.getLatestRankings(keywordId));
    }
}
