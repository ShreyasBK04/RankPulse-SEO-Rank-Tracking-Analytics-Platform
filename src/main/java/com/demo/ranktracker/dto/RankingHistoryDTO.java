package com.demo.ranktracker.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record RankingHistoryDTO(
    Long keywordId,
    String keyword,
    Integer currentPosition,
    Integer bestPosition,
    List<DataPoint> history
) {
    public record DataPoint(LocalDate date, Integer position) {}
}
