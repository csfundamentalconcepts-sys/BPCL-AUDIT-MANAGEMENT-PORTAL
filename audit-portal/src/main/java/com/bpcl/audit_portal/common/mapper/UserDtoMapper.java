package com.bpcl.audit_portal.common.mapper;

import com.bpcl.audit_portal.common.dto.UserDto;
import com.bpcl.audit_portal.common.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public UserDto toDto(User user){

        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();

        dto.setId(user.getId());

        dto.setUserName(user.getUserName());
        dto.setFullName(user.getFullName());

        if (user.getRole() != null) {
            dto.setRole(user.getRole().getRoleName().name());
        }

        dto.setApplicationIds(
                user.getApplications()
                        .stream()
                        .map(app -> app.getId())
                        .toList()
        );

        return dto;
    }
}