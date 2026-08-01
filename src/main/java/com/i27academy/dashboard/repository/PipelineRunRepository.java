package com.i27academy.dashboard.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.i27academy.dashboard.model.PipelineRun;
import com.i27academy.dashboard.model.enums.PipelineStatus;

@Repository
public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {

    List<PipelineRun> findAllByOrderByStartedAtDesc(Pageable pageable);

    @Query("""
        SELECT p FROM PipelineRun p
        WHERE (:serviceId IS NULL OR p.service.id = :serviceId)
          AND (:branch IS NULL OR p.branch = :branch)
          AND (:status IS NULL OR p.status = :status)
        ORDER BY p.startedAt DESC
        """)
    List<PipelineRun> findWithFilters(
            @Param("serviceId") Long serviceId,
            @Param("branch") String branch,
            @Param("status") PipelineStatus status,
            Pageable pageable);

    long countByStatus(PipelineStatus status);

    List<PipelineRun> findTop5ByOrderByStartedAtDesc();
}
