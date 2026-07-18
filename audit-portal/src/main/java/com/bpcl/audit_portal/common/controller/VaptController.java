package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.dto.*;
import com.bpcl.audit_portal.common.model.VaptAudit;
import com.bpcl.audit_portal.common.model.VaptCard;
import com.bpcl.audit_portal.common.service.VaptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.bpcl.audit_portal.common.dto.VaptAuditResponse;

import java.util.List;

@RestController
@RequestMapping("/api/vapt")
public class VaptController {

    private final VaptService vaptService;

    public VaptController(VaptService vaptService) {
        this.vaptService = vaptService;
    }

    @PostMapping("/card")
    public ResponseEntity<VaptCardResponse> createCard(
            @RequestBody CreateVaptCardRequest request,
            @AuthenticationPrincipal UserDetailsImplementation currentUser) {

        VaptCard card = vaptService.createVaptCard(
                request.getApplicationId(),
                currentUser.getId()
        );

        return ResponseEntity.ok(
                VaptCardResponse.builder()
                .id(card.getId())
                .applicationId(card.getApplication().getId())
                .auditInfo(
                        AuditInfoResponse.builder()
                                .userId(card.getCreatedBy().getId())
                                .username(card.getCreatedBy().getUserName())
                                .createdAt(card.getCreatedAt())
                                .build()
                )
                .build()
        );
    }

    @PostMapping("/audit")
    public ResponseEntity<VaptAuditResponse> createAudit(
            @RequestBody CreateVaptAuditRequest request,
            @AuthenticationPrincipal UserDetailsImplementation currentUser) {

        VaptAudit audit = vaptService.createVaptAudit(
                request.getCardId(),
                request.getAuditYear(),
                currentUser.getId()
        );

        return ResponseEntity.ok(
                VaptAuditResponse.builder()
                        .id(audit.getId())
                        .cardId(audit.getVaptCard().getId())
                        .auditYear(audit.getAuditYear())
                        .status(audit.getStatus())
                        .auditInfo(
                                AuditInfoResponse.builder()
                                        .userId(audit.getCreatedBy().getId())
                                        .username(audit.getCreatedBy().getUserName())
                                        .createdAt(audit.getCreatedAt())
                                        .build()
                        )
                        .build()
        );
    }
    @GetMapping("/card/{cardId}/audits")
    public ResponseEntity<List<VaptAuditResponse>> getAuditsByCardId(
            @PathVariable Long cardId) {

        List<VaptAuditResponse> audits = vaptService.getAuditsByCardId(cardId);

        return ResponseEntity.ok(
                audits
        );
    }
    @GetMapping("/card/application/{applicationId}")
    public ResponseEntity<VaptCardResponse> getCardByApplicationId(
            @PathVariable Long applicationId) {

        VaptCardResponse vaptCardResponse = vaptService.getVaptCardByApplicationId(applicationId);

        return ResponseEntity.ok(
                vaptCardResponse
        );
    }
    @PostMapping("/audit/{auditId}/phase")
    public ResponseEntity<?> createPhase(
            @PathVariable Long auditId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password,
            @AuthenticationPrincipal UserDetailsImplementation currentUser
    ) {

        List<VulnerabilityResponse> response =  vaptService.createNextPhase(auditId, file, password, currentUser.getId());
        return ResponseEntity.ok(
              response
        );
    }
    @GetMapping("/audit/{auditId}/phase")
    public ResponseEntity<?> getPhase(
            @PathVariable Long auditId
    ) {

        List<VaptAuditPhaseResponse> response =  vaptService.getPhase(auditId);
        return ResponseEntity.ok(
                response
        );
    }

    @GetMapping("/audit/{phaseId}/vulnerabilities")
    public ResponseEntity<?>getVulnerabilities(@PathVariable Long phaseId){
          List<VulnerabilityResponse> response = vaptService.getVulnerabilities(phaseId);
          return ResponseEntity.ok(response);
    }

    @GetMapping("/vulnerabilities/stats")
    public ResponseEntity<VulnerabilityStatsResponse> VulnerabilityStatsByUser(
            @AuthenticationPrincipal UserDetailsImplementation currentUser) {

        return ResponseEntity.ok(
                vaptService.getVulnerabilityStats(currentUser.getId())
        );
    }
    @GetMapping("/users/{userId}/vulnerabilities/stats")
    public ResponseEntity<VulnerabilityStatsResponse> vulnerabilityStatsByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                vaptService.getVulnerabilityStats(userId)
        );
    }
    @GetMapping("/vulnerabilities/cwe-stats")
    public ResponseEntity<List<CweStatsResponse>> getCweStats() {
        return ResponseEntity.ok(vaptService.getCweStats());
    }
    @GetMapping("/applications/{applicationId}/cwe-stats")
    public ResponseEntity<List<CweStatsResponse>> getApplicationCweStats(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(
                vaptService.getApplicationCweStats(applicationId)
        );
    }
    @GetMapping("/audits/{auditId}/cwe-stats")
    public ResponseEntity<List<CweStatsResponse>> getAuditCweStats(
            @PathVariable Long auditId) {

        return ResponseEntity.ok(
                vaptService.getAuditCweStats(auditId)
        );
    }
    @GetMapping("/phases/{phaseId}/cwe-stats")
    public ResponseEntity<List<CweStatsResponse>> getPhaseCweStats(
            @PathVariable Long phaseId) {

        return ResponseEntity.ok(
                vaptService.getPhaseCweStats(phaseId)
        );
    }
    @GetMapping("/summary")
    public ResponseEntity<VulnerabilityStatsResponse> SystemSummary() {

        return ResponseEntity.ok(
                vaptService.getGlobalSummary()
        );
    }

    @GetMapping("/applications/{applicationId}/summary")
    public ResponseEntity<VulnerabilityStatsResponse> applicationSummary(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(
                vaptService.getApplicationSummary(applicationId)
        );
    }

    @GetMapping("/audits/{auditId}/summary")
    public ResponseEntity<VulnerabilityStatsResponse> auditSummary(
            @PathVariable Long auditId) {

        return ResponseEntity.ok(
                vaptService.getAuditSummary(auditId)
        );
    }

    @GetMapping("/phases/{phaseId}/summary")
    public ResponseEntity<VulnerabilityStatsResponse> phaseSummary(
            @PathVariable Long phaseId) {

        return ResponseEntity.ok(
                vaptService.getPhaseSummary(phaseId)
        );
    }

//    @PatchMapping("/audit/{vulnerabilityId}/vulnerability")
//    public ResponseEntity<?> updateVulnerabilities(@PathVariable Long vulnerabilityId, @RequestBody VulnerabilityUpdateRequest request,@AuthenticationPrincipal UserDetailsImplementation userDetails) {
//
//        AppRole role = userDetails.getAuthorities()
//                .stream()
//                .map(grantedAuthority -> grantedAuthority.getAuthority())
//                .filter(auth -> auth.startsWith("ROLE_"))
//                .map(AppRole::valueOf)
//                .findFirst()
//                .orElse(null);
//        VulnerabilityResponse response = vaptService.updateVulnerability(vulnerabilityId, role , request);
//        return ResponseEntity.ok(response);
//    }

//    @PatchMapping("/audit/{vulnerabilityId}/vulnerability")
//    public ResponseEntity<?> VaptAuditStatusChange(@PathVariable Long vulnerabilityId, @RequestBody VulnerabilityUpdateRequest request) {
//        VulnerabilityResponse response = vaptService.updateVulnerability(vulnerabilityId, request);
//        return ResponseEntity.ok(response);
//    }
}