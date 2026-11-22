package com.demo.ranktracker.dto;

import java.time.LocalDate;
import java.util.List;

public record TrendDataDTO(
    List<DataPoint> dataPoints,
    List<KeywordLine> keywordLines
) {
    public record DataPoint(LocalDate date, Double averagePosition, long keywordsTracked) {}
    public record KeywordLine(Long keywordId, String keyword, List<PositionPoint> positions) {}
    public record PositionPoint(LocalDate date, Integer position) {}
}
