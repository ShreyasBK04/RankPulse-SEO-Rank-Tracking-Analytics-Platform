package com.demo.ranktracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record KeywordDTO(
    Long id,
    Long websiteId,
    String websiteDomain,
    String keyword,
    Integer currentPosition,
    Integer change,
    Integer bestPosition,
    String searchUrl,
    LocalDate lastChecked,
    LocalDateTime createdAt
) {}
