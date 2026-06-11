package com.bpcl.audit_portal.common.service;

import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.dto.ApplicationResponse;
import com.bpcl.audit_portal.common.dto.CreateApplicationRequest;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.mapper.ApplicationMapper;
import com.bpcl.audit_portal.common.model.Application;
import com.bpcl.audit_portal.common.model.User;
import com.bpcl.audit_portal.common.repository.ApplicationRepository;
import com.bpcl.audit_portal.common.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            UserRepository userRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    public ApplicationResponse createApplication(
            CreateApplicationRequest request,
            Long userId
    ) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BAMPException(Errors.MANDATORY_FIELD_MISSING);
        }

        boolean exists = applicationRepository
                .findByName(request.getName())
                .isPresent();

        if (exists) {
            throw new BAMPException(Errors.APPLICATION_ALREADY_EXISTS);
        }

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));

        Application application = Application.builder()
                .name(request.getName())
                .createdBy(currentUser)
                .build();

        Application savedApplication = applicationRepository.save(application);

        AppRole role = currentUser.getRole().getRoleName();
        List<User> heads = currentUser.getAssignedToUsers();
        if (heads != null && !heads.isEmpty()) {
            for (User head : heads) {
                head.getApplications().add(savedApplication);
            }
        }
        return ApplicationMapper.toDto(savedApplication);
    }
    public List<ApplicationResponse> getAllAssignedApplications(Long userId) {

        return applicationRepository.findApplicationsByUserId(userId)
                .stream()
                .map(ApplicationMapper::toDto)
                .toList();
    }
}