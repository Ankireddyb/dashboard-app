package com.i27academy.dashboard.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.i27academy.dashboard.dto.PipelineRunRequest;
import com.i27academy.dashboard.dto.PipelineRunResponse;
import com.i27academy.dashboard.dto.PipelineStatusUpdate;
import com.i27academy.dashboard.model.enums.PipelineStatus;
import com.i27academy.dashboard.service.PipelineService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pipelines")
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping
    public ResponseEntity<PipelineRunResponse> create(@Valid @RequestBody PipelineRunRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pipelineService.createRun(req));
    }

    @GetMapping
    public ResponseEntity<List<PipelineRunResponse>> list(
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) PipelineStatus status,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(pipelineService.listRuns(serviceId, branch, status, skip, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PipelineRunResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(pipelineService.getRun(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PipelineRunResponse> updateStatus(@PathVariable Long id,
                                                             @Valid @RequestBody PipelineStatusUpdate req) {
        return ResponseEntity.ok(pipelineService.updateStatus(id, req));
    }
}
