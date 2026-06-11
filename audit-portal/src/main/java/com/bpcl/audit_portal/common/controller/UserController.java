package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.auth.controller.AuthController;
import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.common.dto.UserDto;
import com.bpcl.audit_portal.common.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private  final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/parentUsers")
    public List<UserDto> parentUsers(@AuthenticationPrincipal UserDetailsImplementation userDetails){
        return userService.getParentUsers(userDetails.getId());
    }
    @GetMapping("/childUsers")
    public List<UserDto> childUsers(@AuthenticationPrincipal UserDetailsImplementation userDtails){
        return userService.getChildUsers(userDtails.getId());
    }

}
