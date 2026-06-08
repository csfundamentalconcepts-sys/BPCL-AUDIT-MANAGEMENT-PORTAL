package com.bpcl.audit_portal.common.service;
import com.bpcl.audit_portal.common.constants.VaptAuditStatus;
import com.bpcl.audit_portal.common.dto.AuditInfoResponse;
import com.bpcl.audit_portal.common.dto.VaptAuditResponse;
import com.bpcl.audit_portal.common.dto.VaptCardResponse;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.model.Application;
import com.bpcl.audit_portal.common.model.User;
import com.bpcl.audit_portal.common.model.VaptAudit;
import com.bpcl.audit_portal.common.model.VaptCard;
import com.bpcl.audit_portal.common.repository.ApplicationRepository;
import com.bpcl.audit_portal.common.repository.UserRepository;
import com.bpcl.audit_portal.common.repository.VaptAuditRepository;
import com.bpcl.audit_portal.common.repository.VaptCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VaptService {

    private final VaptCardRepository vaptCardRepository;
    private final ApplicationRepository applicationRepository;
    private final VaptAuditRepository vaptAuditRepository;
    private final UserRepository userRepository;

    public VaptService(VaptCardRepository vaptCardRepository, ApplicationRepository applicationRepository, VaptAuditRepository vaptAuditRepository, UserRepository userRepository) {
        this.vaptCardRepository = vaptCardRepository;
        this.applicationRepository = applicationRepository;
        this.vaptAuditRepository = vaptAuditRepository;
        this.userRepository = userRepository;
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

}