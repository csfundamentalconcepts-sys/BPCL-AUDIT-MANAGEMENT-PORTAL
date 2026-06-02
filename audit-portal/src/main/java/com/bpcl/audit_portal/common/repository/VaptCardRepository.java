package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.model.VaptCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VaptCardRepository extends JpaRepository<VaptCard, Long> {

    Optional<VaptCard> findByApplicationId(Long applicationId);

    boolean existsByApplicationId(Long applicationId);


}