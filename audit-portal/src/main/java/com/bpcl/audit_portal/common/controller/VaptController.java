package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
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

    @GetMapping("/audit/{phaseId}/vulnerabilities")
    public ResponseEntity<?>getVulnerabilities(@PathVariable Long phaseId){
          List<VulnerabilityResponse> response = vaptService.getVulnerabilities(phaseId);
          return ResponseEntity.ok(response);
    }

//    @PatchMapping("/audit/{vulnerabilityId}/vulnerability")
//    public ResponseEntity<?>updateVulnerabilities(@PathVariable Long vulnerabilityId,@RequestBody VulnerabilityUpdateRequest request){
//         VulnerabilityResponse response = vaptService.updateVulnerability(vulnerabilityId,request);
//         return ResponseEntity.ok(response);
//    }
//
//    @PatchMapping("/audit/{vulnerabilityId}/vulnerability")
//    public ResponseEntity<?>VaptAuditStatusChange(@PathVariable Long vulnerabilityId,@RequestBody VulnerabilityUpdateRequest request){
//        VulnerabilityResponse response = vaptService.updateVulnerability(vulnerabilityId,request);
//        return ResponseEntity.ok(response);
//    }
}