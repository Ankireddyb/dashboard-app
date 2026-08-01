package com.i27academy.dashboard.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.i27academy.dashboard.dto.ServiceRequest;
import com.i27academy.dashboard.dto.ServiceResponse;
import com.i27academy.dashboard.service.ServiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceService.createService(req));
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> list(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(serviceService.listServices(skip, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.getService(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ServiceResponse> update(@PathVariable Long id,
                                                   @RequestBody ServiceRequest req) {
        return ResponseEntity.ok(serviceService.updateService(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
