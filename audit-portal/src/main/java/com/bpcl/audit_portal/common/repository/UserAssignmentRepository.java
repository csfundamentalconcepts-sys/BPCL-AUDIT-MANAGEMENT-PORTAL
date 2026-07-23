package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.constants.TicketStatus;
import com.bpcl.audit_portal.common.model.UserAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserAssignmentRepository
        extends JpaRepository<UserAssignment, Long> {

    Optional<UserAssignment> findByParentUserIdAndChildUserIdAndActiveTrue(
            Long parentUserId,
            Long childUserId
    );

    List<UserAssignment> findByParentUserIdAndActiveTrue(
            Long parentUserId
    );

    List<UserAssignment> findByChildUserIdAndActiveTrue(
            Long childUserId
    );

    boolean existsByParentUserIdAndChildUserIdAndActiveTrue(
            Long parentUserId,
            Long childUserId
    );
}
