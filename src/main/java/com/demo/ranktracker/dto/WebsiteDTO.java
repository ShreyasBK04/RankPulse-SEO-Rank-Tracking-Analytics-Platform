package com.demo.ranktracker.dto;

import java.time.LocalDateTime;

public record WebsiteDTO(
    Long id,
    String domain,
    String name,
    long keywordCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
