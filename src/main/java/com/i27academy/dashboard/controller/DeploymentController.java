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

import com.i27academy.dashboard.dto.DeploymentRequest;
import com.i27academy.dashboard.dto.DeploymentResponse;
import com.i27academy.dashboard.dto.DeploymentStatusUpdate;
import com.i27academy.dashboard.model.enums.DeploymentStatus;
import com.i27academy.dashboard.model.enums.Environment;
import com.i27academy.dashboard.service.DeploymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/deployments")
public class DeploymentController {

    private final DeploymentService deploymentService;

    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @PostMapping
    public ResponseEntity<DeploymentResponse> create(@Valid @RequestBody DeploymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deploymentService.createDeployment(req));
    }

    @GetMapping
    public ResponseEntity<List<DeploymentResponse>> list(
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Environment environment,
            @RequestParam(required = false) DeploymentStatus status,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(deploymentService.listDeployments(serviceId, environment, status, skip, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeploymentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(deploymentService.getDeployment(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeploymentResponse> updateStatus(@PathVariable Long id,
                                                            @Valid @RequestBody DeploymentStatusUpdate req) {
        return ResponseEntity.ok(deploymentService.updateStatus(id, req));
    }
}
