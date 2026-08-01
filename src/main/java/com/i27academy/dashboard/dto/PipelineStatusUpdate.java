package com.i27academy.dashboard.dto;

import com.i27academy.dashboard.model.enums.PipelineStatus;

import jakarta.validation.constraints.NotNull;

public record PipelineStatusUpdate(
        @NotNull(message = "status is required")
        PipelineStatus status,
        Integer durationSeconds
) {}
