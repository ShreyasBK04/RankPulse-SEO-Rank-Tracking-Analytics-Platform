package com.demo.ranktracker.dto;

import com.demo.ranktracker.entity.AlertType;
import java.time.LocalDateTime;

public record AlertDTO(
    Long id,
    Long keywordId,
    String keyword,
    String websiteDomain,
    AlertType alertType,
    String message,
    Integer oldPosition,
    Integer newPosition,
    boolean read,
    LocalDateTime createdAt
) {}
