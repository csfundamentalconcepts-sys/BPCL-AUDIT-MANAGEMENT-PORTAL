package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.model.ApplicationAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationAssignmentRepository
        extends JpaRepository<ApplicationAssignment, Long> {

    boolean existsByApplicationIdAndAssignedToIdAndActiveTrue(
            Long applicationId,
            Long userId
    );

    Optional<ApplicationAssignment>
    findByApplicationIdAndAssignedToIdAndActiveTrue(
            Long applicationId,
            Long userId
    );

    List<ApplicationAssignment>
    findByAssignedToIdAndActiveTrue(Long userId);
}