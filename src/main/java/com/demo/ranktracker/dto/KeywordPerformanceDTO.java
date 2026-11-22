package com.demo.ranktracker.dto;

import java.time.LocalDate;

public record KeywordPerformanceDTO(
    Long keywordId,
    String keyword,
    Integer currentPosition,
    Integer previousPosition,
    Integer change,
    Integer bestPosition,
    String searchUrl,
    LocalDate lastChecked,
    String trend
) {}
