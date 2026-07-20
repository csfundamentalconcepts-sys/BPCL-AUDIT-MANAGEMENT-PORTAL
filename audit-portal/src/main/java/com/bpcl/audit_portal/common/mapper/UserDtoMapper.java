package com.bpcl.audit_portal.common.mapper;

import com.bpcl.audit_portal.common.dto.UserDto;
import com.bpcl.audit_portal.common.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setFullName(user.getFullName());

        if (user.getRole() != null && user.getRole().getRoleName() != null) {
            dto.setRole(user.getRole().getRoleName().name());
        }

        return dto;
    }
}