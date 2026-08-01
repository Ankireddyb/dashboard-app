package com.i27academy.dashboard.dto;

import java.time.LocalDateTime;

import com.i27academy.dashboard.model.PipelineRun;
import com.i27academy.dashboard.model.enums.PipelineStatus;

public record PipelineRunResponse(
        Long id,
        Long serviceId,
        String pipelineName,
        String branch,
        PipelineStatus status,
        String triggeredBy,
        String commitSha,
        String commitMessage,
        Integer durationSeconds,
        String runUrl,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
    public static PipelineRunResponse from(PipelineRun p) {
        return new PipelineRunResponse(
                p.getId(),
                p.getService().getId(),
                p.getPipelineName(),
                p.getBranch(),
                p.getStatus(),
                p.getTriggeredBy(),
                p.getCommitSha(),
                p.getCommitMessage(),
                p.getDurationSeconds(),
                p.getRunUrl(),
                p.getStartedAt(),
                p.getCompletedAt());
    }
}
