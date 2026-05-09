package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.constants.AppPermission;
import com.bpcl.audit_portal.common.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(AppPermission name);

    List<Permission> findByNameIn(List<AppPermission> names);

    boolean existsByName(AppPermission name);
}