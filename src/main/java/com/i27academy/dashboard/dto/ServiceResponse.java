package com.i27academy.dashboard.dto;

import java.time.LocalDateTime;

import com.i27academy.dashboard.model.Service;

public record ServiceResponse(
        Long id,
        String name,
        String description,
        String repositoryUrl,
        String ownerTeam,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ServiceResponse from(Service s) {
        return new ServiceResponse(
                s.getId(), s.getName(), s.getDescription(),
                s.getRepositoryUrl(), s.getOwnerTeam(),
                s.getCreatedAt(), s.getUpdatedAt());
    }
}
