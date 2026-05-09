package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByRoleName(AppRole appRole);

    Boolean existsByRoleName(AppRole appRole);
}

