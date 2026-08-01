package com.i27academy.dashboard.dto;

import java.time.LocalDateTime;

import com.i27academy.dashboard.model.Deployment;
import com.i27academy.dashboard.model.enums.DeploymentStatus;
import com.i27academy.dashboard.model.enums.Environment;

public record DeploymentResponse(
        Long id,
        Long serviceId,
        String version,
        Environment environment,
        DeploymentStatus status,
        String triggeredBy,
        String commitSha,
        String notes,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
    public static DeploymentResponse from(Deployment d) {
        return new DeploymentResponse(
                d.getId(),
                d.getService().getId(),
                d.getVersion(),
                d.getEnvironment(),
                d.getStatus(),
                d.getTriggeredBy(),
                d.getCommitSha(),
                d.getNotes(),
                d.getStartedAt(),
                d.getCompletedAt());
    }
}
