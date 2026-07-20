package com.bpcl.audit_portal.common.service;
import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.dto.UserDto;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.mapper.UserDtoMapper;
import com.bpcl.audit_portal.common.model.Role;
import com.bpcl.audit_portal.common.model.User;
import com.bpcl.audit_portal.common.model.UserAssignment;
import com.bpcl.audit_portal.common.repository.RoleRepository;
import com.bpcl.audit_portal.common.repository.UserAssignmentRepository;
import com.bpcl.audit_portal.common.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bpcl.audit_portal.auth.service.Util.validateUserAssignment;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserAssignmentRepository userAssignmentRepository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserDtoMapper userDtoMapper,UserAssignmentRepository userAssignmentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userAssignmentRepository = userAssignmentRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream().toList();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));
    }

    @Transactional
    public void updateUserRole(Long userId, String roleName) {
        try {
            User user = userRepository.findById(userId).orElseThrow(()
                    -> new BAMPException(Errors.USER_NOT_FOUND));
            AppRole appRole= AppRole.valueOf(roleName);
            Role role = roleRepository.findByRoleName(appRole)
                    .orElseThrow(() -> new BAMPException(Errors.ROLE_NOT_FOUND));
            user.setRole(role);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            log.error("Invalid role provided while updating user role. userId={}, roleName={}", userId, roleName, e);
            throw new BAMPException(Errors.ROLE_NOT_FOUND);
        }
    }
    
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Long userId) {

        User user = findById(userId);

        return UserDtoMapper.toDto(user);
    }

    public User findById(Long id){
        return userRepository.findById(id).orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<UserDto> getParentUsers(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        return userAssignmentRepository
                .findByChildUserIdAndActiveTrue(userId)
                .stream()
                .map(UserAssignment::getParentUser)
                .map(UserDtoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserDto> getChildUsers(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        return userAssignmentRepository
                .findByParentUserIdAndActiveTrue(userId)
                .stream()
                .map(UserAssignment::getChildUser)
                .map(UserDtoMapper::toDto)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<UserDto> getReportingToMe(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();

        queue.add(userId);

        while (!queue.isEmpty()) {

            Long currentUserId = queue.poll();

            List<UserAssignment> assignments =
                    userAssignmentRepository
                            .findByParentUserIdAndActiveTrue(
                                    currentUserId
                            );

            for (UserAssignment assignment : assignments) {

                Long childId =
                        assignment.getChildUser().getId();

                if (visited.add(childId)) {
                    queue.add(childId);
                }
            }
        }

        if (visited.isEmpty()) {
            return Collections.emptyList();
        }

        return userRepository
                .findAllById(visited)
                .stream()
                .map(UserDtoMapper::toDto)
                .toList();
    }

    @Transactional
    public void assignUser(
            Long parentUserId,
            Long childUserId
    ) {

        User parentUser = userRepository
                .findById(parentUserId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        User childUser = userRepository
                .findById(childUserId)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        AppRole parentRole =
                parentUser.getRole().getRoleName();

        AppRole childRole =
                childUser.getRole().getRoleName();

        validateUserAssignment(
                parentRole,
                childRole
        );

        if (userAssignmentRepository
                .existsByParentUserIdAndChildUserIdAndActiveTrue(
                        parentUserId,
                        childUserId
                )) {

            throw new BAMPException(
                    Errors.USER_ALREADY_ASSIGNED
            );
        }

        UserAssignment assignment =
                UserAssignment.builder()
                        .parentUser(parentUser)
                        .childUser(childUser)
                        .assignedBy(parentUser)
                        .active(true)
                        .build();

        userAssignmentRepository.save(
                assignment
        );
    }
    @Transactional
    public void deassignUser(
            Long parentUserId,
            Long childUserId,
            Long performedBy
    ) {

        UserAssignment assignment =
                userAssignmentRepository
                        .findByParentUserIdAndChildUserIdAndActiveTrue(
                                parentUserId,
                                childUserId
                        )
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.USER_ASSIGNMENT_NOT_FOUND
                                ));

        User deassignedBy =
                userRepository.findById(
                        performedBy
                ).orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        assignment.setActive(false);
        assignment.setDeassignedAt(
                LocalDateTime.now()
        );
        assignment.setDeassignedBy(
                deassignedBy
        );

        userAssignmentRepository.save(
                assignment
        );
    }
}
