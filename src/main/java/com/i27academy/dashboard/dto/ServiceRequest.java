package com.i27academy.dashboard.dto;

import jakarta.validation.constraints.NotBlank;

public record ServiceRequest(
        @NotBlank(message = "Service name is required")
        String name,
        String description,
        String repositoryUrl,
        String ownerTeam
) {}
