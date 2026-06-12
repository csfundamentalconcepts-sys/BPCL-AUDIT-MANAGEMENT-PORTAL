package com.bpcl.audit_portal.common.service;
import com.bpcl.audit_portal.auth.controller.AuthController;
import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.common.constants.VaptAuditStatus;
import com.bpcl.audit_portal.common.constants.VaptPhaseStatus;
import com.bpcl.audit_portal.common.dto.AuditInfoResponse;
import com.bpcl.audit_portal.common.dto.VaptAuditResponse;
import com.bpcl.audit_portal.common.dto.VaptCardResponse;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
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

    public void createNextPhase(
            Long auditId,
            MultipartFile file,
            String password,
            Long userId) {
        User currentUser = userRepository.findById(userId).orElseThrow( ()-> new BAMPException(Errors.USER_NOT_FOUND));
        VaptAudit audit = vaptAuditRepository.findById(auditId).orElseThrow(() -> new BAMPException(Errors.VAPT_AUDIT_NOT_FOUND));
        VaptAuditPhase lastPhase = vaptAuditPhaseRepository.findTopByVaptAudit_IdOrderByPhaseNumberDesc(auditId).orElse(null);
        if (lastPhase != null && lastPhase.getStatus() != VaptPhaseStatus.CLOSED) {
            throw new BAMPException(Errors.PREVIOUS_PHASE_NOT_COMPLETED);
        }
        Integer nextPhase =
                (lastPhase == null)
                        ? 1
                        : lastPhase.getPhaseNumber() + 1;
        VaptAuditPhase phase = VaptAuditPhase.builder()
                .phaseNumber(nextPhase)
                .status(VaptPhaseStatus.OPEN)
                .vaptAudit(audit)
                .createdBy(currentUser)
                .build();
        phase = vaptAuditPhaseRepository.save(phase);
        List<Map<String, Object>> response = webClient.post()
                .uri("/parse-pdf")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(
                        BodyInserters.fromMultipartData(
                                "file",
                                file.getResource()
                        ).with("password", password)
                )
                .retrieve()
                .bodyToMono(
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                )
                .block();
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        if (response != null)  {
            log.info("Success");
            for (Map<String, Object> v : response) {
                Vulnerability vuln = new Vulnerability();
                log.info("Here is ",v.get("Vulnerability ID"));
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
        if(vulnerabilities.isEmpty()){
            log.info("Everything is empty");
        }
        vulnerabilityRepository.saveAll(vulnerabilities);
    }
}