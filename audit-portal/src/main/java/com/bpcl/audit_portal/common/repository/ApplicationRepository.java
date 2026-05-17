package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByName(String name);
}
