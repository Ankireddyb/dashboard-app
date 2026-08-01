package com.i27academy.dashboard.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.i27academy.dashboard.dto.ServiceRequest;
import com.i27academy.dashboard.dto.ServiceResponse;
import com.i27academy.dashboard.exception.DuplicateResourceException;
import com.i27academy.dashboard.exception.ResourceNotFoundException;
import com.i27academy.dashboard.model.enums.DeploymentStatus;
import com.i27academy.dashboard.repository.DeploymentRepository;
import com.i27academy.dashboard.repository.ServiceRepository;

@Service
@Transactional
public class ServiceService {

    private final ServiceRepository serviceRepo;
    private final DeploymentRepository deploymentRepo;

    public ServiceService(ServiceRepository serviceRepo, DeploymentRepository deploymentRepo) {
        this.serviceRepo = serviceRepo;
        this.deploymentRepo = deploymentRepo;
    }

    public ServiceResponse createService(ServiceRequest req) {
        if (serviceRepo.existsByName(req.name())) {
            throw new DuplicateResourceException("Service with name '" + req.name() + "' already exists");
        }
        var entity = new com.i27academy.dashboard.model.Service();
        entity.setName(req.name());
        entity.setDescription(req.description());
        entity.setRepositoryUrl(req.repositoryUrl());
        entity.setOwnerTeam(req.ownerTeam());
        return ServiceResponse.from(serviceRepo.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> listServices(int skip, int limit) {
        return serviceRepo.findAll(PageRequest.of(skip / Math.max(limit, 1), limit))
                .map(ServiceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse getService(Long id) {
        return serviceRepo.findById(id)
                .map(ServiceResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
    }

    public ServiceResponse updateService(Long id, ServiceRequest req) {
        var entity = serviceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
        if (req.description() != null) entity.setDescription(req.description());
        if (req.repositoryUrl() != null) entity.setRepositoryUrl(req.repositoryUrl());
        if (req.ownerTeam() != null) entity.setOwnerTeam(req.ownerTeam());
        return ServiceResponse.from(serviceRepo.save(entity));
    }

    public void deleteService(Long id) {
        if (!serviceRepo.existsById(id)) {
            throw new ResourceNotFoundException("Service not found: " + id);
        }
        serviceRepo.deleteById(id);
    }
}
