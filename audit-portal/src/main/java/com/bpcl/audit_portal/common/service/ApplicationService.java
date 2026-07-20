package com.bpcl.audit_portal.common.service;

import com.bpcl.audit_portal.common.dto.ApplicationResponse;
import com.bpcl.audit_portal.common.dto.CreateApplicationRequest;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.mapper.ApplicationMapper;
import com.bpcl.audit_portal.common.model.Application;
import com.bpcl.audit_portal.common.model.ApplicationAssignment;
import com.bpcl.audit_portal.common.model.User;
import com.bpcl.audit_portal.common.repository.ApplicationAssignmentRepository;
import com.bpcl.audit_portal.common.repository.ApplicationRepository;
import com.bpcl.audit_portal.common.repository.UserAssignmentRepository;
import com.bpcl.audit_portal.common.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationAssignmentRepository applicationAssignmentRepository;
    private final UserAssignmentRepository userAssignmentRepository;
    private final UserRepository userRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            ApplicationAssignmentRepository applicationAssignmentRepository,
            UserAssignmentRepository userAssignmentRepository,
            UserRepository userRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.applicationAssignmentRepository = applicationAssignmentRepository;
        this.userAssignmentRepository = userAssignmentRepository;
        this.userRepository = userRepository;
    }

    public ApplicationResponse createApplication(
            CreateApplicationRequest request,
            Long userId
    ) {

        if (request.getName() == null ||
                request.getName().isBlank()) {

            throw new BAMPException(
                    Errors.MANDATORY_FIELD_MISSING
            );
        }

        if (applicationRepository
                .findByName(request.getName())
                .isPresent()) {

            throw new BAMPException(
                    Errors.APPLICATION_ALREADY_EXISTS
            );
        }

        User currentUser = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        Application application = Application.builder()
                .name(request.getName())
                .createdBy(currentUser)
                .build();

        applicationRepository.save(application);

        return ApplicationMapper.toDto(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAssignedApplications(
            Long userId
    ) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        return applicationAssignmentRepository
                .findByAssignedToIdAndActiveTrue(userId)
                .stream()
                .map(ApplicationAssignment::getApplication)
                .map(ApplicationMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {

        return applicationRepository.findAll()
                .stream()
                .map(ApplicationMapper::toDto)
                .toList();
    }

    public void assignApplication(
            Long applicationId,
            Long targetUserId,
            Long currentUserId
    ) {

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.APPLICATION_NOT_FOUND
                                ));

        User currentUser =
                userRepository.findById(currentUserId)
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.USER_NOT_FOUND
                                ));

        User targetUser =
                userRepository.findById(targetUserId)
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.USER_NOT_FOUND
                                ));

        boolean isChild =
                userAssignmentRepository
                        .existsByParentUserIdAndChildUserIdAndActiveTrue(
                                currentUserId,
                                targetUserId
                        );

        if (!isChild) {
            throw new BAMPException(
                    Errors.INVALID_ASSIGNMENT
            );
        }

        boolean alreadyAssigned =
                applicationAssignmentRepository
                        .existsByApplicationIdAndAssignedToIdAndActiveTrue(
                                applicationId,
                                targetUserId
                        );

        if (alreadyAssigned) {
            throw new BAMPException(
                    Errors.APPLICATION_ALREADY_ASSIGNED
            );
        }

        ApplicationAssignment assignment =
                ApplicationAssignment.builder()
                        .application(application)
                        .assignedTo(targetUser)
                        .assignedBy(currentUser)
                        .active(true)
                        .build();

        applicationAssignmentRepository.save(
                assignment
        );
    }

    public void deassignApplication(
            Long applicationId,
            Long targetUserId,
            Long currentUserId
    ) {

        ApplicationAssignment assignment =
                applicationAssignmentRepository
                        .findByApplicationIdAndAssignedToIdAndActiveTrue(
                                applicationId,
                                targetUserId
                        )
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.APPLICATION_NOT_ASSIGNED
                                ));

        boolean isChild =
                userAssignmentRepository
                        .existsByParentUserIdAndChildUserIdAndActiveTrue(
                                currentUserId,
                                targetUserId
                        );

        if (!isChild) {
            throw new BAMPException(
                    Errors.INVALID_ASSIGNMENT
            );
        }

        User currentUser =
                userRepository.findById(currentUserId)
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.USER_NOT_FOUND
                                ));

        assignment.setActive(false);
        assignment.setDeassignedAt(LocalDateTime.now());
        assignment.setDeassignedBy(currentUser);

        applicationAssignmentRepository.save(
                assignment
        );
    }
}