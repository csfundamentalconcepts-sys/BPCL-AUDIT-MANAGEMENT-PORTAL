package com.bpcl.audit_portal.common.service;
import com.bpcl.audit_portal.common.constants.*;
import com.bpcl.audit_portal.common.dto.*;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.mapper.VaptAuditPhaseMapper;
import com.bpcl.audit_portal.common.mapper.VulnerabilityMapper;
import com.bpcl.audit_portal.common.model.*;
import com.bpcl.audit_portal.common.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VaptService {

    private static final Logger log = LoggerFactory.getLogger(VaptService.class);
    private final VaptCardRepository vaptCardRepository;
    private final ApplicationRepository applicationRepository;
    private final VaptAuditRepository vaptAuditRepository;
    private final UserRepository userRepository;
    private final VaptAuditPhaseRepository vaptAuditPhaseRepository;
    private final WebClient webClient;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final VulnerabilityAssignmentRepository vulnerabilityAssignmentRepository;

    public VaptService(
            VaptCardRepository vaptCardRepository,
            ApplicationRepository applicationRepository,
            VaptAuditRepository vaptAuditRepository, UserRepository userRepository,
            VaptAuditPhaseRepository vaptAuditPhaseRepository,
            VulnerabilityRepository vulnerabilityRepository,
            VulnerabilityAssignmentRepository vulnerabilityAssignmentRepository,
            WebClient webClient) {

        this.vaptCardRepository = vaptCardRepository;
        this.applicationRepository = applicationRepository;
        this.vaptAuditRepository = vaptAuditRepository;
        this.userRepository = userRepository;
        this.vaptAuditPhaseRepository = vaptAuditPhaseRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.webClient = webClient;
        this.vulnerabilityAssignmentRepository = vulnerabilityAssignmentRepository;
    }

    @Transactional
    public void assignVulnerability(
            Long vulnerabilityId,
            Long developerId,
            Long currentUserId) {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new BAMPException(Errors.USER_NOT_FOUND));

        if (currentUser.getRole().getRoleName() != AppRole.SCRUM_MASTER) {
            throw new BAMPException(
                    Errors.INVALID_VULNERABILITY_ASSIGNMENT);
        }

        User developer = userRepository.findById(developerId)
                .orElseThrow(() ->
                        new BAMPException(Errors.USER_NOT_FOUND));

        if (developer.getRole().getRoleName() != AppRole.DEVELOPER) {
            throw new BAMPException(
                    Errors.INVALID_VULNERABILITY_ASSIGNMENT);
        }

        Vulnerability vulnerability =
                vulnerabilityRepository.findById(vulnerabilityId)
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.VULNERABILITY_NOT_FOUND));

        vulnerabilityAssignmentRepository
                .findByVulnerabilityIdAndActiveTrue(vulnerabilityId)
                .ifPresent(existing -> {
                    throw new BAMPException(
                            Errors.USER_ALREADY_ASSIGNED);
                });

        VulnerabilityAssignment assignment =
                VulnerabilityAssignment.builder()
                        .vulnerability(vulnerability)
                        .assignedTo(developer)
                        .assignedBy(currentUser)
                        .active(true)
                        .build();

        vulnerabilityAssignmentRepository.save(assignment);
    }

    @Transactional
    public VaptCardResponse createVaptCard(
            Long applicationId,
            Long userId) {

        if (vaptCardRepository.existsByApplicationId(applicationId)) {
            throw new BAMPException(Errors.VAPT_CARD_ALREADY_EXISTS);
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new BAMPException(Errors.APPLICATION_NOT_FOUND));

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BAMPException(Errors.USER_NOT_FOUND));

        VaptCard card = VaptCard.builder()
                .application(application)
                .createdBy(currentUser)
                .build();

        card = vaptCardRepository.save(card);

        return VaptCardResponse.builder()
                .id(card.getId())
                .applicationId(card.getApplication().getId())
                .auditInfo(
                        AuditInfoResponse.builder()
                                .userId(card.getCreatedBy().getId())
                                .username(card.getCreatedBy().getUserName())
                                .createdAt(card.getCreatedAt())
                                .build()
                )
                .build();
    }
    @Transactional
    public VaptAuditResponse createVaptAudit(
            Long cardId,
            Integer auditYear,
            Long userId) {

        if (vaptAuditRepository.existsByVaptCardIdAndAuditYear(
                cardId,
                auditYear)) {

            throw new BAMPException(
                    Errors.VAPT_AUDIT_ALREADY_EXISTS);
        }

        VaptCard card = vaptCardRepository.findById(cardId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.VAPT_CARD_NOT_FOUND));

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND));

        VaptAudit audit = VaptAudit.builder()
                .vaptCard(card)
                .auditYear(auditYear)
                .status(VaptAuditStatus.OPEN)
                .createdBy(currentUser)
                .build();

        audit = vaptAuditRepository.save(audit);

        return VaptAuditResponse.builder()
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
                .build();
    }

    @Transactional(readOnly = true)
    public VaptCardResponse getVaptCardByApplicationId(Long applicationId) {

        VaptCard card = vaptCardRepository.findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new BAMPException(Errors.VAPT_CARD_NOT_FOUND));

        return VaptCardResponse.builder()
                .id(card.getId())
                .applicationId(card.getApplication().getId())
                .auditInfo(
                        AuditInfoResponse.builder()
                                .userId(card.getCreatedBy().getId())
                                .username(card.getCreatedBy().getUserName())
                                .createdAt(card.getCreatedAt())
                                .build()
                )
                .build();
    }

    @Transactional(readOnly = true)
    public List<VaptAuditResponse> getAuditsByCardId(Long cardId) {

        if (!vaptCardRepository.existsById(cardId)) {
            throw new BAMPException(Errors.VAPT_CARD_NOT_FOUND);
        }

        List<VaptAudit> vaptAudits = vaptAuditRepository.findByVaptCardIdOrderByAuditYearDesc(cardId);
        return vaptAudits.stream()
                .map(audit -> VaptAuditResponse.builder()
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
                )
                .toList();
    }

    @Transactional
    public VaptAuditPhase createPhase(Long auditId, Long userId) {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));

        VaptAudit audit = vaptAuditRepository.findById(auditId)
                .orElseThrow(() -> new BAMPException(Errors.VAPT_AUDIT_NOT_FOUND));

        VaptAuditPhase lastPhase =
                vaptAuditPhaseRepository.findTopByVaptAudit_IdOrderByPhaseNumberDesc(auditId)
                        .orElse(null);

        if (lastPhase != null && lastPhase.getStatus() != VaptPhaseStatus.CLOSED) {
            throw new BAMPException(Errors.PREVIOUS_PHASE_NOT_COMPLETED);
        }

        int nextPhase = (lastPhase == null) ? 1 : lastPhase.getPhaseNumber() + 1;

        VaptAuditPhase phase = VaptAuditPhase.builder()
                .phaseNumber(nextPhase)
                .status(VaptPhaseStatus.OPEN)
                .vaptAudit(audit)
                .createdBy(currentUser)
                .build();

        return vaptAuditPhaseRepository.save(phase);
    }

    public List<Map<String, Object>> parsePdf(MultipartFile file, String password) {

        return webClient.post()
                .uri("/parse-pdf")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", file.getResource())
                        .with("password", password))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
    }

    private NewOrRepeat determineNewOrRepeat(
            VaptAuditPhase phase,
            String vulnerabilityId) {

        Long cardId =
                phase.getVaptAudit()
                        .getVaptCard()
                        .getId();

        List<VaptAudit> audits =
                vaptAuditRepository
                        .findByVaptCardIdOrderByAuditYearDesc(cardId);

        for (VaptAudit audit : audits) {

            if (audit.getId().equals(
                    phase.getVaptAudit().getId())) {
                continue;
            }

            List<VaptAuditPhase> phases =
                    vaptAuditPhaseRepository
                            .findByVaptAuditId(audit.getId());

            for (VaptAuditPhase previous : phases) {

                boolean exists =
                        vulnerabilityRepository
                                .findByVaptAuditPhaseId(
                                        previous.getId())
                                .stream()
                                .anyMatch(v ->
                                        vulnerabilityId.equals(
                                                v.getVulnerabilityId()));

                if (exists) {
                    return NewOrRepeat.REPEAT;
                }
            }
        }

        return NewOrRepeat.NEW;
    }

    private Vulnerability buildVulnerability(
            Map<String, Object> row,
            VaptAuditPhase phase) {

        Vulnerability vulnerability =
                new Vulnerability();

        vulnerability.setVulnerabilityId(
                (String) row.get("Vulnerability ID"));

        vulnerability.setAffectedAsset(
                (String) row.get("Affected Asset"));

        vulnerability.setName(
                (String) row.get("Nameof the Vulnerability"));

        vulnerability.setDetailedObservation(
                (String) row.get("Detailed observation"));

        vulnerability.setCveCwe(
                (String) row.get("CVE/CWE"));

        vulnerability.setCvss(
                (String) row.get("CVSS"));

        vulnerability.setEpss(
                (String) row.get("EPSS"));

        vulnerability.setSeverity(
                (String) row.get("Severity"));

        vulnerability.setStatus(
                VulnerabilityStatus.valueOf(
                        ((String) row.get("Status"))
                                .trim()
                                .toUpperCase()
                ));

        vulnerability.setNewOrRepeat(
                determineNewOrRepeat(
                        phase,
                        (String) row.get("Vulnerability ID")
                ));

        vulnerability.setRecommendation(
                (String) row.get("Recommendation"));

        vulnerability.setReference(
                (String) row.get("Reference"));

        vulnerability.setVaptAuditPhase(phase);

        return vulnerability;
    }
    private VulnerabilityAssignment findPreviousAssignment(
            String vulnerabilityExternalId,
            VaptAuditPhase currentPhase) {

        Long cardId =
                currentPhase.getVaptAudit()
                        .getVaptCard()
                        .getId();

        List<VaptAudit> audits =
                vaptAuditRepository
                        .findByVaptCardIdOrderByAuditYearDesc(cardId);

        for (VaptAudit audit : audits) {

            List<VaptAuditPhase> phases =
                    vaptAuditPhaseRepository
                            .findByVaptAuditId(audit.getId());

            for (VaptAuditPhase phase : phases) {

                List<Vulnerability> vulnerabilities =
                        vulnerabilityRepository
                                .findByVaptAuditPhaseId(
                                        phase.getId());

                for (Vulnerability vulnerability : vulnerabilities) {

                    if (!vulnerabilityExternalId.equals(
                            vulnerability.getVulnerabilityId())) {
                        continue;
                    }

                    return vulnerabilityAssignmentRepository
                            .findByVulnerabilityIdAndActiveTrue(
                                    vulnerability.getId())
                            .orElse(null);
                }
            }
        }

        return null;
    }
    private void handleAssignmentMigration(
            Vulnerability vulnerability,
            VaptAuditPhase currentPhase) {

        VulnerabilityAssignment previousAssignment =
                findPreviousAssignment(
                        vulnerability.getVulnerabilityId(),
                        currentPhase
                );

        if (previousAssignment == null) {
            return;
        }

        VulnerabilityAssignment assignment =
                VulnerabilityAssignment.builder()
                        .vulnerability(vulnerability)
                        .assignedTo(
                                previousAssignment.getAssignedTo())
                        .assignedBy(
                                previousAssignment.getAssignedBy())
                        .active(true)
                        .build();

        vulnerabilityAssignmentRepository.save(
                assignment
        );
    }
    @Transactional
    public List<Vulnerability> saveVulnerabilities(
            List<Map<String, Object>> parsed,
            VaptAuditPhase phase) {

        List<Vulnerability> saved = new ArrayList<>();

        for (Map<String, Object> row : parsed) {

            Vulnerability vulnerability =
                    buildVulnerability(row, phase);

            vulnerability =
                    vulnerabilityRepository.save(vulnerability);

            handleAssignmentMigration(
                    vulnerability,
                    phase
            );

            saved.add(vulnerability);
        }

        return saved;
    }

    @Transactional
    public List<VulnerabilityResponse> createNextPhase(
            Long auditId,
            MultipartFile file,
            String password,
            Long userId) {

        VaptAuditPhase phase =
                createPhase(auditId, userId);

        List<Map<String, Object>> parsed =
                parsePdf(file, password);

        List<Vulnerability> saved =
                saveVulnerabilities(parsed, phase);

        return saved.stream()
                .map(VulnerabilityMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VulnerabilityResponse> getVulnerabilities(Long phaseId) {

        if (!vaptAuditPhaseRepository.existsById(phaseId)) {
            throw new BAMPException(Errors.VAPT_PHASE_NOT_FOUND);
        }

        return vulnerabilityRepository.findByVaptAuditPhaseId(phaseId)
                .stream()
                .map(vulnerability -> {

                    VulnerabilityAssignment assignment =
                            vulnerabilityAssignmentRepository
                                    .findByVulnerabilityIdAndActiveTrue(
                                            vulnerability.getId()
                                    )
                                    .orElse(null);

                    return VulnerabilityMapper.toResponse(
                            vulnerability,
                            assignment
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VaptAuditPhaseResponse> getPhase(Long auditId) {
         if(!vaptAuditRepository.existsById(auditId)){
             throw new BAMPException(Errors.VAPT_AUDIT_NOT_FOUND);
         }
         return vaptAuditPhaseRepository.findByVaptAuditId(auditId)
                 .stream()
                 .map(VaptAuditPhaseMapper :: toResponse)
                 .toList();
    }

    @Transactional(readOnly = true)
    public VulnerabilityStatsResponse getVulnerabilityStats(Long userId) {

        Object[] stats = vulnerabilityRepository.getVulnerabilityStatsByUser(userId);

        return VulnerabilityStatsResponse.builder()
                .total(((Number) stats[0]).longValue())
                .open(((Number) stats[1]).longValue())
                .closed(((Number) stats[2]).longValue())
                .notPasrsed(((Number) stats[3]).longValue())
                .build();
    }

    @Transactional(readOnly = true)
    public VulnerabilityStatsResponse getSystemVulnerabilityStats() {

        Object[] stats = vulnerabilityRepository.getSystemVulnerabilityStats();

        return VulnerabilityStatsResponse.builder()
                .total(((Number) stats[0]).longValue())
                .open(((Number) stats[1]).longValue())
                .closed(((Number) stats[2]).longValue())
                .notPasrsed(((Number) stats[3]).longValue())
                .build();
    }
    @Transactional(readOnly = true)
    public List<CweStatsResponse> getCweStats() {

        return vulnerabilityRepository.getCweStats(NewOrRepeat.NEW,VulnerabilityStatus.OPEN)
                .stream()
                .map(row -> CweStatsResponse.builder()
                        .cwe((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
    }
    @Transactional(readOnly = true)
    public List<CweStatsResponse> getApplicationCweStats(Long applicationId) {

        return vulnerabilityRepository
                .getApplicationCweStats(applicationId, NewOrRepeat.NEW,VulnerabilityStatus.OPEN)
                .stream()
                .map(row -> CweStatsResponse.builder()
                        .cwe((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
    }
    @Transactional(readOnly = true)
    public List<CweStatsResponse> getAuditCweStats(Long auditId) {

        return vulnerabilityRepository
                .getAuditCweStats(auditId, NewOrRepeat.NEW,VulnerabilityStatus.OPEN)
                .stream()
                .map(row -> CweStatsResponse.builder()
                        .cwe((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
    }
    @Transactional(readOnly = true)
    public List<CweStatsResponse> getPhaseCweStats(Long phaseId) {

        return vulnerabilityRepository
                .getPhaseCweStats(phaseId, NewOrRepeat.NEW,VulnerabilityStatus.OPEN)
                .stream()
                .map(row -> CweStatsResponse.builder()
                        .cwe((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public VulnerabilityStatsResponse getGlobalSummary() {

        return VulnerabilityStatsMapper.toResponse(
                vulnerabilityRepository.getSystemSummary()
        );
    }

    @Transactional(readOnly = true)
    public VulnerabilityStatsResponse getApplicationSummary(Long applicationId) {

        return VulnerabilityStatsMapper.toResponse(
                vulnerabilityRepository.getApplicationSummary(applicationId)
        );
    }

    @Transactional(readOnly = true)
    public VulnerabilityStatsResponse getAuditSummary(Long auditId) {

        return VulnerabilityStatsMapper.toResponse(
                vulnerabilityRepository.getAuditSummary(auditId)
        );
    }

    @Transactional(readOnly = true)
    public VulnerabilityStatsResponse getPhaseSummary(Long phaseId) {

        return VulnerabilityStatsMapper.toResponse(
                vulnerabilityRepository.getPhaseSummary(phaseId)
        );
    }
    @Transactional
    public VulnerabilityResponse updateVulnerability(
            Long vulnerabilityId,
            VulnerabilityUpdateRequest request) {

        Vulnerability vulnerability = vulnerabilityRepository.findById(vulnerabilityId)
                .orElseThrow(() ->
                        new RuntimeException("Vulnerability not found"));

        if (request.getVulnerabilityId() != null) {
            vulnerability.setVulnerabilityId(request.getVulnerabilityId());
        }

        if (request.getAffectedAsset() != null) {
            vulnerability.setAffectedAsset(request.getAffectedAsset());
        }

        if (request.getName() != null) {
            vulnerability.setName(request.getName());
        }

        if (request.getDetailedObservation() != null) {
            vulnerability.setDetailedObservation(request.getDetailedObservation());
        }

        if (request.getCveCwe() != null) {
            vulnerability.setCveCwe(request.getCveCwe());
        }

        if (request.getCvss() != null) {
            vulnerability.setCvss(request.getCvss());
        }

        if (request.getEpss() != null) {
            vulnerability.setEpss(request.getEpss());
        }

        if (request.getSeverity() != null) {
            vulnerability.setSeverity(request.getSeverity());
        }

        if (request.getStatus() != null) {
            vulnerability.setStatus(request.getStatus());
        }

        if (request.getVulnerabilityStatusByUser() != null) {
            vulnerability.setVulnerabilityStatusByUser(
                    request.getVulnerabilityStatusByUser());
        }

        if (request.getRecommendation() != null) {
            vulnerability.setRecommendation(request.getRecommendation());
        }

        if (request.getReference() != null) {
            vulnerability.setReference(request.getReference());
        }

        if (request.getNewOrRepeat() != null) {
            vulnerability.setNewOrRepeat(request.getNewOrRepeat());
        }

        vulnerabilityRepository.save(vulnerability);

        return VulnerabilityMapper.toResponse(vulnerability);
    }

    @Transactional
    public void closePhase(Long phaseId) {
        VaptAuditPhase phase = vaptAuditPhaseRepository.findById(phaseId).orElseThrow(() -> new BAMPException(Errors.VAPT_PHASE_NOT_FOUND));
        long pending = vulnerabilityRepository.countOpenNotFixedVulnerabilities(
                phaseId,
                VulnerabilityStatus.OPEN,
                VulnerabilityStatusByUser.FIXED
        );
        if (pending > 0) {
            throw new BAMPException(Errors.VAPT_PHASE_CANNOT_BE_CLOSED);
        }
        phase.setStatus(VaptPhaseStatus.CLOSED);
        vaptAuditPhaseRepository.save(phase);
    }

    @Transactional
    public void closeAudit(Long auditId) {

        VaptAudit audit = vaptAuditRepository.findById(auditId).orElseThrow(() -> new BAMPException(Errors.VAPT_AUDIT_NOT_FOUND));

        VaptAuditPhase lastPhase = vaptAuditPhaseRepository.findTopByVaptAudit_IdOrderByPhaseNumberDesc(auditId).orElseThrow(() -> new BAMPException(Errors.VAPT_PHASE_NOT_FOUND));

        long remaining = vulnerabilityRepository.countNonClosedVulnerabilities(
                lastPhase.getId(),
                VulnerabilityStatus.CLOSED
        );
        if (remaining > 0) {

            throw new BAMPException(Errors.VAPT_AUDIT_CANNOT_BE_CLOSED);
        }
        audit.setStatus(VaptAuditStatus.CLOSED);
        vaptAuditRepository.save(audit);
    }
}