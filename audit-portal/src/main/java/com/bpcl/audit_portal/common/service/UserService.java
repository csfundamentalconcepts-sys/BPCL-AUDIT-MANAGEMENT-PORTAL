package com.bpcl.audit_portal.common.service;

import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.model.Role;
import com.bpcl.audit_portal.common.model.User;
import com.bpcl.audit_portal.common.repository.RoleRepository;
import com.bpcl.audit_portal.common.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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

    public User findById(Long id){
        return userRepository.findById(id).orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
