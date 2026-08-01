package com.i27academy.dashboard.dto;

import com.i27academy.dashboard.model.enums.DeploymentStatus;

import jakarta.validation.constraints.NotNull;

public record DeploymentStatusUpdate(
        @NotNull(message = "status is required")
        DeploymentStatus status,
        String notes
) {}
