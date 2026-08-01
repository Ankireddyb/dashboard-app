package com.i27academy.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.i27academy.dashboard.dto.DeploymentRequest;
import com.i27academy.dashboard.dto.DeploymentResponse;
import com.i27academy.dashboard.dto.DeploymentStatusUpdate;
import com.i27academy.dashboard.exception.ResourceNotFoundException;
import com.i27academy.dashboard.model.Deployment;
import com.i27academy.dashboard.model.enums.DeploymentStatus;
import com.i27academy.dashboard.model.enums.Environment;
import com.i27academy.dashboard.repository.DeploymentRepository;
import com.i27academy.dashboard.repository.ServiceRepository;

@Service
@Transactional
public class DeploymentService {

    private static final Set<DeploymentStatus> TERMINAL = Set.of(
            DeploymentStatus.SUCCESS, DeploymentStatus.FAILED, DeploymentStatus.CANCELLED);

    private final DeploymentRepository deploymentRepo;
    private final ServiceRepository serviceRepo;

    public DeploymentService(DeploymentRepository deploymentRepo, ServiceRepository serviceRepo) {
        this.deploymentRepo = deploymentRepo;
        this.serviceRepo = serviceRepo;
    }

    public DeploymentResponse createDeployment(DeploymentRequest req) {
        var service = serviceRepo.findById(req.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + req.serviceId()));
        var d = new Deployment();
        d.setService(service);
        d.setVersion(req.version());
        d.setEnvironment(req.environment());
        d.setTriggeredBy(req.triggeredBy());
        d.setCommitSha(req.commitSha());
        d.setNotes(req.notes());
        return DeploymentResponse.from(deploymentRepo.save(d));
    }

    @Transactional(readOnly = true)
    public List<DeploymentResponse> listDeployments(Long serviceId, Environment environment,
                                                     DeploymentStatus status, int skip, int limit) {
        return deploymentRepo.findWithFilters(serviceId, environment, status,
                        PageRequest.of(skip / Math.max(limit, 1), limit))
                .stream().map(DeploymentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DeploymentResponse getDeployment(Long id) {
        return deploymentRepo.findById(id)
                .map(DeploymentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment not found: " + id));
    }

    public DeploymentResponse updateStatus(Long id, DeploymentStatusUpdate req) {
        var d = deploymentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment not found: " + id));
        d.setStatus(req.status());
        if (TERMINAL.contains(req.status())) d.setCompletedAt(LocalDateTime.now());
        if (req.notes() != null) d.setNotes(req.notes());
        return DeploymentResponse.from(deploymentRepo.save(d));
    }
}
