package com.bpcl.audit_portal.common.service;
import com.bpcl.audit_portal.common.constants.*;
import com.bpcl.audit_portal.common.dto.*;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.mapper.VaptAuditPhaseMapper;
import com.bpcl.audit_portal.common.mapper.VulnerabilityAssignmentMapper;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final VaptVulnerabilityHistoryRepository vulnerabilityHistoryRepository;
    private final VulnerabilityAssignmentMapper vulnerabilityAssignmentMapper;

    public VaptService(
            VaptCardRepository vaptCardRepository,
            ApplicationRepository applicationRepository,
            VaptAuditRepository vaptAuditRepository, UserRepository userRepository,
            VaptAuditPhaseRepository vaptAuditPhaseRepository,
            VulnerabilityRepository vulnerabilityRepository,
            VulnerabilityAssignmentRepository vulnerabilityAssignmentRepository,
            VaptVulnerabilityHistoryRepository vulnerabilityHistoryRepository,
            VulnerabilityAssignmentMapper vulnerabilityAssignmentMapper,
            WebClient webClient) {

        this.vaptCardRepository = vaptCardRepository;
        this.applicationRepository = applicationRepository;
        this.vaptAuditRepository = vaptAuditRepository;
        this.userRepository = userRepository;
        this.vaptAuditPhaseRepository = vaptAuditPhaseRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.webClient = webClient;
        this.vulnerabilityAssignmentRepository = vulnerabilityAssignmentRepository;
        this.vulnerabilityHistoryRepository = vulnerabilityHistoryRepository;
        this.vulnerabilityAssignmentMapper=vulnerabilityAssignmentMapper;
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
            Long userId) {

        if (vaptAuditRepository.existsByVaptCardId(
                cardId)) {

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
                .status(VaptAuditStatus.OPEN)
                .createdBy(currentUser)
                .build();

        audit = vaptAuditRepository.save(audit);

        return VaptAuditResponse.builder()
                .id(audit.getId())
                .cardId(audit.getVaptCard().getId())
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

        List<VaptAudit> vaptAudits = vaptAuditRepository.findByVaptCardId(cardId);
        return vaptAudits.stream()
                .map(audit -> VaptAuditResponse.builder()
                        .id(audit.getId())
                        .cardId(audit.getVaptCard().getId())
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

        boolean exists =
                vulnerabilityRepository.existsInPreviousAudits(
                        phase.getVaptAudit().getVaptCard().getId(),
                        phase.getVaptAudit().getId(),
                        vulnerabilityId
                );

        return exists
                ? NewOrRepeat.REPEAT
                : NewOrRepeat.NEW;
    }

    private String getString(Map<String, Object> row, String key) {
        return Objects.toString(row.get(key), "").trim();
    }
    private Vulnerability buildVulnerability(
            Map<String, Object> row,
            VaptAuditPhase phase,
            User createdBy) {

        Vulnerability vulnerability = new Vulnerability();

        vulnerability.setVulnerabilityId(
                getString(row, "Vulnerability ID"));

        vulnerability.setAffectedAsset(
                getString(row, "Affected Asset"));

        vulnerability.setName(
                getString(row, "Nameof the Vulnerability"));

        vulnerability.setDetailedObservation(
                getString(row, "Detailed observation"));

        vulnerability.setCveCwe(
                getString(row, "CVE/CWE"));

        vulnerability.setCvss(
                getString(row, "CVSS"));

        vulnerability.setEpss(
                getString(row, "EPSS"));

        vulnerability.setSeverity(
                getString(row, "Severity"));

        String status = getString(row, "Status");

        if (status.isBlank()) {
            vulnerability.setStatus(VulnerabilityStatus.NOT_PARSED);
        } else {
            try {
                vulnerability.setStatus(
                        VulnerabilityStatus.valueOf(
                                status.trim()
                                        .toUpperCase()
                                        .replace(" ", "_")
                        )
                );
            } catch (IllegalArgumentException ex) {
                vulnerability.setStatus(VulnerabilityStatus.NOT_PARSED);
            }
        }

        vulnerability.setNewOrRepeat(
                determineNewOrRepeat(
                        phase,
                        getString(row, "Vulnerability ID")
                )
        );

        vulnerability.setRecommendation(
                getString(row, "Recommendation"));

        vulnerability.setReference(
                getString(row, "Reference"));

        vulnerability.setVaptAuditPhase(phase);

        Object pointsObj = row.get("Vulnerability Point");

        if (pointsObj instanceof List<?> points) {

            List<VulnerabilityPoint> vulnerabilityPoints =
                    points.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .map(value -> VulnerabilityPoint.builder()
                                    .value(value)
                                    .vulnerability(vulnerability)
                                    .build())
                            .toList();

            vulnerability.setVulnerabilityPoints(
                    new ArrayList<>(vulnerabilityPoints));
        }

        vulnerability.setCreatedBy(createdBy);

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
                        .findByVaptCardId(cardId);

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
            VaptAuditPhase phase,
            Long userId) {

        List<Vulnerability> saved = new ArrayList<>();

        User user = userRepository.findById(userId).orElseThrow(()-> new BAMPException(Errors.USER_NOT_FOUND));

        for (Map<String, Object> row : parsed) {

            Vulnerability vulnerability =
                    buildVulnerability(row, phase,user);

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
    public List<VulnerabilityResponse> createNextPhase(
            Long auditId,
            MultipartFile file,
            String password,
            Long userId) {

        List<Map<String, Object>> parsed =
                parsePdf(file, password);

        return createNextPhaseInternal(
                auditId,
                parsed,
                userId
        );
    }
    @Transactional
    public List<VulnerabilityResponse> createNextPhaseInternal(
            Long auditId,
            List<Map<String, Object>> parsed,
            Long userId) {

        VaptAuditPhase phase = createPhase(
                auditId,
                userId
        );

        List<Vulnerability> saved =
                saveVulnerabilities(parsed, phase,userId);

        return saved.stream()
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

        if (stats == null || stats.length < 4) {
            return VulnerabilityStatsResponse.builder()
                    .total(0L)
                    .open(0L)
                    .closed(0L)
                    .notPasrsed(0L)
                    .build();
        }

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

        return vulnerabilityRepository
                .getCweStats(
                        NewOrRepeat.NEW,
                        VulnerabilityStatus.OPEN
                )
                .stream()
                .map(row -> CweStatsResponse.builder()
                        .cveCwe((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
    }
    @Transactional(readOnly = true)
    public List<CweStatsResponse> getApplicationCweStats(
            Long applicationId
    ) {

        return vulnerabilityRepository
                .getApplicationCweStats(
                        applicationId,
                        NewOrRepeat.NEW,
                        VulnerabilityStatus.OPEN
                )
                .stream()
                .map(row -> CweStatsResponse.builder()
                        .cveCwe((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
    }
    @Transactional(readOnly = true)
    public List<CweStatsResponse> getAuditCweStats(
            Long auditId
    ) {

        return vulnerabilityRepository
                .getAuditCweStats(
                        auditId,
                        NewOrRepeat.NEW,
                        VulnerabilityStatus.OPEN
                )
                .stream()
                .map(row -> CweStatsResponse.builder()
                        .cveCwe((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
    }
    @Transactional(readOnly = true)
    public List<CweStatsResponse> getPhaseCweStats(
            Long phaseId
    ) {

        return vulnerabilityRepository
                .getPhaseCweStats(
                        phaseId,
                        NewOrRepeat.NEW,
                        VulnerabilityStatus.OPEN
                )
                .stream()
                .map(row -> CweStatsResponse.builder()
                        .cveCwe((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public VulnerabilityStatsResponse getGlobalSummary() {

        Object[] stats = vulnerabilityRepository
                .getSystemSummary()
                .get(0);

        return VulnerabilityStatsMapper.toResponse(stats);
    }

    @Transactional(readOnly = true)
    public VulnerabilityStatsResponse getApplicationSummary(Long applicationId) {

        Object[] stats = vulnerabilityRepository
                .getApplicationSummary(applicationId)
                .get(0);

        return VulnerabilityStatsMapper.toResponse(stats);
    }

    @Transactional(readOnly = true)
    public VulnerabilityStatsResponse getAuditSummary(Long auditId) {

        Object[] stats = vulnerabilityRepository
                .getAuditSummary(auditId)
                .get(0);

        return VulnerabilityStatsMapper.toResponse(stats);
    }

    @Transactional(readOnly = true)
    public VulnerabilityStatsResponse getPhaseSummary(Long phaseId) {

        Object result = vulnerabilityRepository.getPhaseSummary(phaseId);

        return VulnerabilityStatsMapper.toResponse((Object[]) result);
    }
    @Transactional
    public VulnerabilityResponse updateVulnerability(
            Long vulnerabilityId,
            VulnerabilityUpdateRequest request,
            Long userId
    ) {

        Vulnerability vulnerability = vulnerabilityRepository
                .findById(vulnerabilityId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.VULNERABILITY_NOT_FOUND
                        ));

        User updatedBy = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        if (request.getVulnerabilityId() != null &&
                !request.getVulnerabilityId().equals(
                        vulnerability.getVulnerabilityId())) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("vulnerabilityId")
                            .oldValue(vulnerability.getVulnerabilityId())
                            .newValue(request.getVulnerabilityId())
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setVulnerabilityId(
                    request.getVulnerabilityId()
            );
        }

        if (request.getAffectedAsset() != null &&
                !request.getAffectedAsset().equals(
                        vulnerability.getAffectedAsset())) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("affectedAsset")
                            .oldValue(vulnerability.getAffectedAsset())
                            .newValue(request.getAffectedAsset())
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setAffectedAsset(
                    request.getAffectedAsset()
            );
        }

        if (request.getName() != null &&
                !request.getName().equals(
                        vulnerability.getName())) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("name")
                            .oldValue(vulnerability.getName())
                            .newValue(request.getName())
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setName(
                    request.getName()
            );
        }

        if (request.getDetailedObservation() != null &&
                !request.getDetailedObservation().equals(
                        vulnerability.getDetailedObservation())) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("detailedObservation")
                            .oldValue(vulnerability.getDetailedObservation())
                            .newValue(request.getDetailedObservation())
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setDetailedObservation(
                    request.getDetailedObservation()
            );
        }

        if (request.getCveCwe() != null &&
                !request.getCveCwe().equals(
                        vulnerability.getCveCwe())) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("cveCwe")
                            .oldValue(vulnerability.getCveCwe())
                            .newValue(request.getCveCwe())
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setCveCwe(
                    request.getCveCwe()
            );
        }

        if (request.getCvss() != null &&
                !request.getCvss().equals(
                        vulnerability.getCvss())) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("cvss")
                            .oldValue(vulnerability.getCvss())
                            .newValue(request.getCvss())
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setCvss(
                    request.getCvss()
            );
        }

        if (request.getEpss() != null &&
                !request.getEpss().equals(
                        vulnerability.getEpss())) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("epss")
                            .oldValue(vulnerability.getEpss())
                            .newValue(request.getEpss())
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setEpss(
                    request.getEpss()
            );
        }

        if (request.getSeverity() != null &&
                !request.getSeverity().equals(
                        vulnerability.getSeverity())) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("severity")
                            .oldValue(vulnerability.getSeverity())
                            .newValue(request.getSeverity())
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setSeverity(
                    request.getSeverity()
            );
        }

        if (request.getStatus() != null &&
                request.getStatus() != vulnerability.getStatus()) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("status")
                            .oldValue(vulnerability.getStatus().name())
                            .newValue(request.getStatus().name())
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setStatus(
                    request.getStatus()
            );
        }

        if (request.getVulnerabilityStatusByUser() != null &&
                request.getVulnerabilityStatusByUser()
                        != vulnerability.getVulnerabilityStatusByUser()) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("vulnerabilityStatusByUser")
                            .oldValue(
                                    vulnerability
                                            .getVulnerabilityStatusByUser()
                                            .name()
                            )
                            .newValue(
                                    request
                                            .getVulnerabilityStatusByUser()
                                            .name()
                            )
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setVulnerabilityStatusByUser(
                    request.getVulnerabilityStatusByUser()
            );
        }

        if (request.getRecommendation() != null &&
                !request.getRecommendation().equals(
                        vulnerability.getRecommendation())) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("recommendation")
                            .oldValue(vulnerability.getRecommendation())
                            .newValue(request.getRecommendation())
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setRecommendation(
                    request.getRecommendation()
            );
        }

        if (request.getReference() != null &&
                !request.getReference().equals(
                        vulnerability.getReference())) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("reference")
                            .oldValue(vulnerability.getReference())
                            .newValue(request.getReference())
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setReference(
                    request.getReference()
            );
        }

        if (request.getNewOrRepeat() != null &&
                request.getNewOrRepeat()
                        != vulnerability.getNewOrRepeat()) {

            vulnerabilityHistoryRepository.save(
                    VaptVulnerabilityHistory.builder()
                            .vulnerability(vulnerability)
                            .fieldName("newOrRepeat")
                            .oldValue(
                                    vulnerability.getNewOrRepeat().name()
                            )
                            .newValue(
                                    request.getNewOrRepeat().name()
                            )
                            .updatedBy(updatedBy)
                            .build()
            );

            vulnerability.setNewOrRepeat(
                    request.getNewOrRepeat()
            );
        }

        vulnerability = vulnerabilityRepository.save(
                vulnerability
        );

        VulnerabilityAssignment assignment =
                vulnerabilityAssignmentRepository
                        .findByVulnerabilityIdAndActiveTrue(
                                vulnerabilityId
                        )
                        .orElse(null);

        return VulnerabilityMapper.toResponse(
                vulnerability,
                assignment
        );
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
    @Transactional
    public void deassignVulnerability(
            Long vulnerabilityId,
            Long userId
    ) {

        Vulnerability vulnerability =
                vulnerabilityRepository
                        .findById(vulnerabilityId)
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.VULNERABILITY_NOT_FOUND
                                ));

        User currentUser =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.USER_NOT_FOUND
                                ));

        VulnerabilityAssignment assignment =
                vulnerabilityAssignmentRepository
                        .findByVulnerabilityIdAndActiveTrue(
                                vulnerabilityId
                        )
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.VULNERABILITY_NOT_ASSIGNED
                                ));

        if (!assignment.getAssignedBy().getId()
                .equals(userId)) {

            throw new BAMPException(
                    Errors.UNAUTHORIZED
            );
        }

        assignment.setActive(false);
        assignment.setDeassignedAt(LocalDateTime.now());
        assignment.setDeassignedBy(currentUser);

        vulnerabilityAssignmentRepository.save(
                assignment
        );
    }
    @Transactional(readOnly = true)
    public List<VulnerabilityResponse>
    getAssignedVulnerabilities(
            Long userId
    ) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        List<VulnerabilityAssignment> assignments =
                vulnerabilityAssignmentRepository
                        .findByAssignedToIdAndActiveTrue(
                                userId
                        );

        return assignments.stream()
                .map(assignment ->
                        VulnerabilityMapper.toResponse(
                                assignment.getVulnerability(),
                                assignment
                        )
                )
                .toList();
    }
    @Transactional(readOnly = true)
    public List<VulnerabilityAssignmentResponse>
    getAssignmentHistory(
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

        return vulnerabilityAssignmentRepository
                .findByVulnerabilityIdOrderByAssignedAtDesc(
                        vulnerabilityId
                )
                .stream()
                .map(vulnerabilityAssignmentMapper::toResponse)
                .toList();
    }
}