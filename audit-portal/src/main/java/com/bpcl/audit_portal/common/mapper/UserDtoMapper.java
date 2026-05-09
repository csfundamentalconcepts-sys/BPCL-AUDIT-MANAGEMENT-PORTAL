package com.bpcl.audit_portal.common.mapper;


import com.bpcl.audit_portal.common.dto.UserDto;
import com.bpcl.audit_portal.common.model.User;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class UserDtoMapper {

    public UserDto toDto(User user){
        if (user == null) {
            return null;
        }

        UserDto userdto = new UserDto();
        userdto.setId(user.getId());
        userdto.setUserName(user.getUserName());

        if (user.getRole() != null) {
            userdto.setRole(user.getRole().getRoleName().name());
        }

        if (user.getPermissions() != null) {
            userdto.setPermissions(
                    user.getPermissions()
                            .stream()
                            .map(permission -> permission.getName().name())
                            .collect(Collectors.toList())
            );
        }
        return userdto;
    }
}


