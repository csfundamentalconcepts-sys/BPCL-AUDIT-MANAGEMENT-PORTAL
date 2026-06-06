package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByName(String name);

    @Query("""
    SELECT a
    FROM User u
    JOIN u.applications a
    WHERE u.id = :userId
    """)
    List<Application> findApplicationsByUserId(Long userId);
}
