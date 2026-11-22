package com.demo.ranktracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RankingDTO(
    Long id,
    Long keywordId,
    String keyword,
    Integer position,
    String searchUrl,
    LocalDate checkedOn,
    LocalDateTime createdAt
) {}
