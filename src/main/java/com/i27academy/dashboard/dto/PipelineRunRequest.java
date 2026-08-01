package com.i27academy.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PipelineRunRequest(
        @NotNull(message = "service_id is required")
        Long serviceId,
        @NotBlank(message = "pipeline_name is required")
        String pipelineName,
        String branch,
        @NotBlank(message = "triggered_by is required")
        String triggeredBy,
        String commitSha,
        String commitMessage,
        String runUrl
) {}
