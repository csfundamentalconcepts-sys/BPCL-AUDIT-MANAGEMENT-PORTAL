package com.bpcl.audit_portal.common.service;

import com.bpcl.audit_portal.common.dto.VulnerabilityHistoryResponse;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.mapper.VulnerabilityHistoryMapper;
import com.bpcl.audit_portal.common.repository.VaptVulnerabilityHistoryRepository;
import com.bpcl.audit_portal.common.repository.VulnerabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VaptHistoryService {

    private final VaptVulnerabilityHistoryRepository
            vulnerabilityHistoryRepository;

    private final VulnerabilityRepository
            vulnerabilityRepository;

    private final VulnerabilityHistoryMapper
            vulnerabilityHistoryMapper;

    public VaptHistoryService(
            VaptVulnerabilityHistoryRepository vulnerabilityHistoryRepository,
            VulnerabilityRepository vulnerabilityRepository,
            VulnerabilityHistoryMapper vulnerabilityHistoryMapper
    ) {
        this.vulnerabilityHistoryRepository =
                vulnerabilityHistoryRepository;

        this.vulnerabilityRepository =
                vulnerabilityRepository;

        this.vulnerabilityHistoryMapper =
                vulnerabilityHistoryMapper;
    }

    public List<VulnerabilityHistoryResponse>
    getVulnerabilityHistory(
            Long vulnerabilityId
    ) {

        vulnerabilityRepository.findById(
                        vulnerabilityId
                )
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.VULNERABILITY_NOT_FOUND
                        )
                );

        return vulnerabilityHistoryRepository
                .findByVulnerabilityIdOrderByCreatedAtDesc(
                        vulnerabilityId
                )
                .stream()
                .map(vulnerabilityHistoryMapper::toResponse)
                .toList();
    }
}