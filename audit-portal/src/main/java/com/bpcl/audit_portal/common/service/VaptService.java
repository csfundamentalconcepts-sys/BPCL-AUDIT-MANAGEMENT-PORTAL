package com.bpcl.audit_portal.common.service;
import com.bpcl.audit_portal.common.constants.NewOrRepeat;
import com.bpcl.audit_portal.common.constants.VaptAuditStatus;
import com.bpcl.audit_portal.common.constants.VaptPhaseStatus;
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

    public VaptService(
            VaptCardRepository vaptCardRepository,
            ApplicationRepository applicationRepository,
            VaptAuditRepository vaptAuditRepository, UserRepository userRepository,
            VaptAuditPhaseRepository vaptAuditPhaseRepository,
            VulnerabilityRepository vulnerabilityRepository,
            WebClient webClient) {

        this.vaptCardRepository = vaptCardRepository;
        this.applicationRepository = applicationRepository;
        this.vaptAuditRepository = vaptAuditRepository;
        this.userRepository = userRepository;
        this.vaptAuditPhaseRepository = vaptAuditPhaseRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.webClient = webClient;
    }

    @Transactional
    public VaptCard createVaptCard(Long applicationId, Long userId) {

        if (vaptCardRepository.existsByApplicationId(applicationId)) {
            throw new BAMPException(Errors.VAPT_CARD_ALREADY_EXISTS);
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BAMPException(Errors.APPLICATION_NOT_FOUND));

        User currentUser = userRepository.findById(userId).orElseThrow(()-> new BAMPException(Errors.USER_NOT_FOUND));

        VaptCard card = VaptCard.builder()
                .application(application)
                .createdBy(currentUser)
                .build();

        return vaptCardRepository.save(card);
    }

    @Transactional
    public VaptAudit createVaptAudit(Long cardId, Integer auditYear, Long userId){


        if (vaptAuditRepository.existsByVaptCardIdAndAuditYear(cardId, auditYear)) {
            throw new BAMPException(Errors.VAPT_AUDIT_ALREADY_EXISTS);
        }

        VaptCard card = vaptCardRepository.findById(cardId)
                .orElseThrow(() -> new BAMPException(Errors.VAPT_CARD_NOT_FOUND));

        User currentUser = userRepository.findById(userId).orElseThrow(()-> new BAMPException(Errors.USER_NOT_FOUND));

        VaptAudit audit = VaptAudit.builder()
                .vaptCard(card)
                .auditYear(auditYear)
                .status(VaptAuditStatus.OPEN)
                .createdBy(currentUser)
                .build();

        return vaptAuditRepository.save(audit);
    }

    @Transactional(readOnly = true)
    public VaptCardResponse getVaptCardByApplicationId(Long applicationId) {

        VaptCard card = vaptCardRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new BAMPException(Errors.VAPT_CARD_NOT_FOUND));

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
    @Transactional
    public List<Vulnerability> saveVulnerabilities(
            List<Map<String, Object>> response,
            VaptAuditPhase phase
    ) {

        List<Vulnerability> vulnerabilities = new ArrayList<>();

        if (response != null) {
            for (Map<String, Object> v : response) {

                Vulnerability vuln = new Vulnerability();
                vuln.setVulnerabilityId((String) v.get("Vulnerability ID"));
                vuln.setAffectedAsset((String) v.get("Affected Asset"));
                vuln.setName((String) v.get("Nameof the Vulnerability"));
                vuln.setDetailedObservation((String) v.get("Detailed observation"));
                vuln.setCveCwe((String) v.get("CVE/CWE"));
                vuln.setCvss((String) v.get("CVSS"));
                vuln.setEpss((String) v.get("EPSS"));
                vuln.setSeverity((String) v.get("Severity"));
                vuln.setStatus((String) v.get("Status"));
                vuln.setRecommendation((String) v.get("Recommendation"));
                vuln.setReference((String) v.get("Reference"));
                vuln.setNewOrRepeat((String) v.get("New or repeat"));
                vuln.setVaptAuditPhase(phase);

                List<String> points = (List<String>) v.get("Vulnerability Point");

                List<VulnerabilityPoint> pointEntities = new ArrayList<>();
                if (points != null) {
                    for (String point : points) {
                        VulnerabilityPoint vp = new VulnerabilityPoint();
                        vp.setValue(point);
                        vp.setVulnerability(vuln);
                        pointEntities.add(vp);
                    }
                }

                vuln.setVulnerabilityPoints(pointEntities);
                vulnerabilities.add(vuln);
            }
        }

        return vulnerabilityRepository.saveAll(vulnerabilities);
    }

    public List<VulnerabilityResponse> createNextPhase(
            Long auditId,
            MultipartFile file,
            String password,
            Long userId
    ) {
        VaptAuditPhase phase = createPhase(auditId, userId);

        List<Map<String, Object>> response = parsePdf(file, password);

        List<Vulnerability> saved = saveVulnerabilities(response, phase);

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
                .map(VulnerabilityMapper::toResponse)
                .toList();
    }

//    public VulnerabilityResponse updateVulnerability(Long vulnerabilityId, AppRole role, VulnerabilityUpdateRequest request) {
//
//    }

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

        return vulnerabilityRepository.getCweStats(NewOrRepeat.NEW)
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
                .getApplicationCweStats(applicationId, NewOrRepeat.NEW)
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
                .getAuditCweStats(auditId, NewOrRepeat.NEW)
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
                .getPhaseCweStats(phaseId, NewOrRepeat.NEW)
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
}