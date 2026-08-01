package com.i27academy.dashboard.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.i27academy.dashboard.dto.DashboardSummary;
import com.i27academy.dashboard.dto.DeploymentResponse;
import com.i27academy.dashboard.dto.PipelineRunResponse;
import com.i27academy.dashboard.model.enums.DeploymentStatus;
import com.i27academy.dashboard.model.enums.PipelineStatus;
import com.i27academy.dashboard.repository.DeploymentRepository;
import com.i27academy.dashboard.repository.PipelineRunRepository;
import com.i27academy.dashboard.repository.ServiceRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ServiceRepository serviceRepo;
    private final DeploymentRepository deploymentRepo;
    private final PipelineRunRepository pipelineRepo;

    public DashboardService(ServiceRepository serviceRepo,
                            DeploymentRepository deploymentRepo,
                            PipelineRunRepository pipelineRepo) {
        this.serviceRepo = serviceRepo;
        this.deploymentRepo = deploymentRepo;
        this.pipelineRepo = pipelineRepo;
    }

    public DashboardSummary getSummary() {
        var deploymentStatusCounts = Map.of(
                "pending", deploymentRepo.countByStatus(DeploymentStatus.PENDING),
                "running", deploymentRepo.countByStatus(DeploymentStatus.RUNNING),
                "success", deploymentRepo.countByStatus(DeploymentStatus.SUCCESS),
                "failed", deploymentRepo.countByStatus(DeploymentStatus.FAILED),
                "cancelled", deploymentRepo.countByStatus(DeploymentStatus.CANCELLED));

        var pipelineStatusCounts = Map.of(
                "queued", pipelineRepo.countByStatus(PipelineStatus.QUEUED),
                "running", pipelineRepo.countByStatus(PipelineStatus.RUNNING),
                "success", pipelineRepo.countByStatus(PipelineStatus.SUCCESS),
                "failed", pipelineRepo.countByStatus(PipelineStatus.FAILED));

        var recentDeployments = deploymentRepo.findTop5ByOrderByStartedAtDesc()
                .stream().map(DeploymentResponse::from).toList();

        var recentPipelineRuns = pipelineRepo.findTop5ByOrderByStartedAtDesc()
                .stream().map(PipelineRunResponse::from).toList();

        return new DashboardSummary(
                serviceRepo.count(),
                deploymentRepo.count(),
                pipelineRepo.count(),
                deploymentStatusCounts,
                pipelineStatusCounts,
                recentDeployments,
                recentPipelineRuns);
    }
}
