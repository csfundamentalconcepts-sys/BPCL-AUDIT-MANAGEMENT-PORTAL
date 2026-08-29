package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.common.dto.*;
import com.bpcl.audit_portal.common.model.VaptAudit;
import com.bpcl.audit_portal.common.model.VaptCard;
import com.bpcl.audit_portal.common.service.VaptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

        return ResponseEntity.ok(
                vaptService.createVaptCard(
                        request.getApplicationId(),
                        currentUser.getId()
                )
        );
    }

    @PostMapping("/audit")
    public ResponseEntity<VaptAuditResponse> createAudit(
            @RequestBody CreateVaptAuditRequest request,
            @AuthenticationPrincipal UserDetailsImplementation currentUser) {

        return ResponseEntity.ok(
                vaptService.createVaptAudit(
                        request.getCardId(),
                        currentUser.getId()
                )
        );
    }

    @GetMapping("/card/{cardId}/audits")
    public ResponseEntity<List<VaptAuditResponse>> getAuditsByCardId(
            @PathVariable Long cardId) {

        return ResponseEntity.ok(
                vaptService.getAuditsByCardId(cardId)
        );
    }

    @GetMapping("/card/application/{applicationId}")
    public ResponseEntity<VaptCardResponse> getCardByApplicationId(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(
                vaptService.getVaptCardByApplicationId(applicationId)
        );
    }

    @PostMapping("/audit/{auditId}/phase")
    public ResponseEntity<?> createPhase(
            @PathVariable Long auditId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password,
            @AuthenticationPrincipal UserDetailsImplementation currentUser){

        return ResponseEntity.ok(
                vaptService.createNextPhase(
                        auditId,
                        file,
                        password,
                        currentUser.getId()
                )
        );
    }
    @GetMapping("/audit/{auditId}/phase")
    public ResponseEntity<?> getPhase(
            @PathVariable Long auditId) {

        return ResponseEntity.ok(
                vaptService.getPhase(auditId)
        );
    }

    @GetMapping("/audit/{phaseId}/vulnerabilities")
    public ResponseEntity<?> getVulnerabilities(
            @PathVariable Long phaseId) {

        return ResponseEntity.ok(
                vaptService.getVulnerabilities(phaseId)
        );
    }

    @GetMapping("/vulnerabilities/stats")
    public ResponseEntity<VulnerabilityStatsResponse> vulnerabilityStatsByUser(
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
    public ResponseEntity<VulnerabilityStatsResponse> systemSummary() {

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

    @PatchMapping("/vulnerabilities/{vulnerabilityId}")
    public ResponseEntity<VulnerabilityResponse> updateVulnerability(
            @PathVariable Long vulnerabilityId,
            @RequestBody VulnerabilityUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImplementation currentUser) {

        return ResponseEntity.ok(
                vaptService.updateVulnerability(
                        vulnerabilityId,
                        request,
                        currentUser.getId()
                )
        );
    }

    @PatchMapping("/phases/{phaseId}/close")
    public ResponseEntity<Void> closePhase(
            @PathVariable Long phaseId) {

        vaptService.closePhase(phaseId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/audits/{auditId}/close")
    public ResponseEntity<Void> closeAudit(
            @PathVariable Long auditId) {

        vaptService.closeAudit(auditId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/vulnerabilities/{vulnerabilityId}/assign")
    @PreAuthorize("hasRole('SCRUM_MASTER')")
    public ResponseEntity<Void> assignVulnerability(
            @PathVariable Long vulnerabilityId,
            @RequestBody AssignVulnerabilityRequest request,
            @AuthenticationPrincipal UserDetailsImplementation currentUser) {

        vaptService.assignVulnerability(
                vulnerabilityId,
                request.getDeveloperId(),
                currentUser.getId()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/me/vulnerabilities")
    public ResponseEntity<List<VulnerabilityResponse>>
    getMyAssignedVulnerabilities(
            @AuthenticationPrincipal UserDetailsImplementation currentUser) {

        return ResponseEntity.ok(
                vaptService.getAssignedVulnerabilities(
                        currentUser.getId()
                )
        );
    }

    @GetMapping("/users/{userId}/vulnerabilities")
    public ResponseEntity<List<VulnerabilityResponse>>
    getAssignedVulnerabilities(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                vaptService.getAssignedVulnerabilities(userId)
        );
    }

    @GetMapping("/vulnerabilities/{vulnerabilityId}/assignments")
    public ResponseEntity<List<VulnerabilityAssignmentResponse>>
    getAssignmentHistory(
            @PathVariable Long vulnerabilityId
    ) {

        return ResponseEntity.ok(
                vaptService.getAssignmentHistory(
                        vulnerabilityId
                )
        );
    }
}
