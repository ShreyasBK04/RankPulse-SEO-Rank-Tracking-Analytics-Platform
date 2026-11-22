package com.demo.ranktracker.controller;

import com.demo.ranktracker.dto.*;
import com.demo.ranktracker.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard/{websiteId}")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(@PathVariable Long websiteId) {
        return ResponseEntity.ok(analyticsService.getDashboardStats(websiteId));
    }

    @GetMapping("/performance/{websiteId}")
    public ResponseEntity<List<KeywordPerformanceDTO>> getKeywordPerformance(
            @PathVariable Long websiteId) {
        return ResponseEntity.ok(analyticsService.getKeywordPerformance(websiteId));
    }

    @GetMapping("/trends/{websiteId}")
    public ResponseEntity<TrendDataDTO> getRankingTrends(
            @PathVariable Long websiteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getRankingTrends(websiteId, startDate, endDate));
    }

    @GetMapping("/distribution/{websiteId}")
    public ResponseEntity<DistributionDTO> getRankingDistribution(@PathVariable Long websiteId) {
        return ResponseEntity.ok(analyticsService.getRankingDistribution(websiteId));
    }
}
