package com.i27academy.dashboard.dto;

import java.util.List;
import java.util.Map;

public record DashboardSummary(
        long totalServices,
        long totalDeployments,
        long totalPipelineRuns,
        Map<String, Long> deploymentStatusCounts,
        Map<String, Long> pipelineStatusCounts,
        List<DeploymentResponse> recentDeployments,
        List<PipelineRunResponse> recentPipelineRuns
) {}
