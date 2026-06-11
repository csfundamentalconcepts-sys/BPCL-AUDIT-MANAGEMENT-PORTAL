package com.bpcl.audit_portal.common.service;
import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.dto.UserDto;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.mapper.UserDtoMapper;
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
    private final UserDtoMapper userDtoMapper;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserDtoMapper userDtoMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userDtoMapper = userDtoMapper;
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

        return userDtoMapper.toDto(user);
    }

    public User findById(Long id){
        return userRepository.findById(id).orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<UserDto> getParentUsers(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new BAMPException(Errors.USER_NOT_FOUND));
        return user.getAssignedToUsers().stream().map(userDtoMapper::toDto).toList();
    }
    @Transactional(readOnly = true)
    public List<UserDto> getChildUsers(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new BAMPException(Errors.USER_NOT_FOUND));
        return userRepository.getChildUsers(userId).stream().map(userDtoMapper::toDto).toList();
    }
}
