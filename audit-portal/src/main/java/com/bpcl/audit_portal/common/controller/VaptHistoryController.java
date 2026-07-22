package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.common.dto.VulnerabilityHistoryResponse;
import com.bpcl.audit_portal.common.service.VaptHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vapt-history")
public class VaptHistoryController {

    private final VaptHistoryService vaptHistoryService;

    public VaptHistoryController(
            VaptHistoryService vaptHistoryService
    ) {
        this.vaptHistoryService = vaptHistoryService;
    }

    @GetMapping("/{vulnerabilityId}")
    public List<VulnerabilityHistoryResponse>
    getVulnerabilityHistory(
            @PathVariable Long vulnerabilityId
    ) {

        return vaptHistoryService
                .getVulnerabilityHistory(
                        vulnerabilityId
                );
    }
}