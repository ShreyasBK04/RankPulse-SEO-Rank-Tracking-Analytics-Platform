package com.demo.ranktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KeywordCreateRequest(
    @NotBlank(message = "Keyword is required")
    @Size(min = 2, max = 255, message = "Keyword must be between 2 and 255 characters")
    String keyword
) {}
