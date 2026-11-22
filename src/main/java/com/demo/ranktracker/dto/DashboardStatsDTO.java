package com.demo.ranktracker.dto;

import java.time.LocalDateTime;

public record DashboardStatsDTO(
    long totalKeywords,
    Double averagePosition,
    long top10Keywords,
    long top100Keywords,
    long improvedKeywords,
    long declinedKeywords,
    LocalDateTime lastChecked
) {}
