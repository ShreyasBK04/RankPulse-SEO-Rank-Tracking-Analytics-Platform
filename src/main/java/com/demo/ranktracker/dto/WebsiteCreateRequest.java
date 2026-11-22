package com.demo.ranktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WebsiteCreateRequest(
    @NotBlank(message = "Domain is required")
    @Size(max = 255, message = "Domain must be 255 characters or fewer")
    @Pattern(regexp = "^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}$",
             message = "Invalid domain format")
    String domain,

    @Size(max = 255, message = "Name must be 255 characters or fewer")
    String name
) {}
