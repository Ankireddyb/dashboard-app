package com.i27academy.dashboard.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.i27academy.dashboard.model.Deployment;
import com.i27academy.dashboard.model.enums.DeploymentStatus;
import com.i27academy.dashboard.model.enums.Environment;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

    List<Deployment> findAllByOrderByStartedAtDesc(Pageable pageable);

    @Query("""
        SELECT d FROM Deployment d
        WHERE (:serviceId IS NULL OR d.service.id = :serviceId)
          AND (:environment IS NULL OR d.environment = :environment)
          AND (:status IS NULL OR d.status = :status)
        ORDER BY d.startedAt DESC
        """)
    List<Deployment> findWithFilters(
            @Param("serviceId") Long serviceId,
            @Param("environment") Environment environment,
            @Param("status") DeploymentStatus status,
            Pageable pageable);

    long countByStatus(DeploymentStatus status);

    List<Deployment> findTop5ByOrderByStartedAtDesc();
}
