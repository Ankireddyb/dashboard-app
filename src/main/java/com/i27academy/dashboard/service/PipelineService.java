package com.i27academy.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.i27academy.dashboard.dto.PipelineRunRequest;
import com.i27academy.dashboard.dto.PipelineRunResponse;
import com.i27academy.dashboard.dto.PipelineStatusUpdate;
import com.i27academy.dashboard.exception.ResourceNotFoundException;
import com.i27academy.dashboard.model.PipelineRun;
import com.i27academy.dashboard.model.enums.PipelineStatus;
import com.i27academy.dashboard.repository.PipelineRunRepository;
import com.i27academy.dashboard.repository.ServiceRepository;

@Service
@Transactional
public class PipelineService {

    private static final Set<PipelineStatus> TERMINAL = Set.of(PipelineStatus.SUCCESS, PipelineStatus.FAILED);

    private final PipelineRunRepository pipelineRepo;
    private final ServiceRepository serviceRepo;

    public PipelineService(PipelineRunRepository pipelineRepo, ServiceRepository serviceRepo) {
        this.pipelineRepo = pipelineRepo;
        this.serviceRepo = serviceRepo;
    }

    public PipelineRunResponse createRun(PipelineRunRequest req) {
        var service = serviceRepo.findById(req.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + req.serviceId()));
        var p = new PipelineRun();
        p.setService(service);
        p.setPipelineName(req.pipelineName());
        p.setBranch(req.branch() != null ? req.branch() : "main");
        p.setTriggeredBy(req.triggeredBy());
        p.setCommitSha(req.commitSha());
        p.setCommitMessage(req.commitMessage());
        p.setRunUrl(req.runUrl());
        return PipelineRunResponse.from(pipelineRepo.save(p));
    }

    @Transactional(readOnly = true)
    public List<PipelineRunResponse> listRuns(Long serviceId, String branch,
                                               PipelineStatus status, int skip, int limit) {
        return pipelineRepo.findWithFilters(serviceId, branch, status,
                        PageRequest.of(skip / Math.max(limit, 1), limit))
                .stream().map(PipelineRunResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PipelineRunResponse getRun(Long id) {
        return pipelineRepo.findById(id)
                .map(PipelineRunResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline run not found: " + id));
    }

    public PipelineRunResponse updateStatus(Long id, PipelineStatusUpdate req) {
        var p = pipelineRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline run not found: " + id));
        p.setStatus(req.status());
        if (TERMINAL.contains(req.status())) p.setCompletedAt(LocalDateTime.now());
        if (req.durationSeconds() != null) p.setDurationSeconds(req.durationSeconds());
        return PipelineRunResponse.from(pipelineRepo.save(p));
    }
}
