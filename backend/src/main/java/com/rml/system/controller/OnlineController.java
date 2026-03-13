package com.rml.system.controller;

import com.rml.system.dto.ApiResponse;
import com.rml.system.dto.response.UserDto;
import com.rml.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OnlineController {

    private final UserService userService;

    @GetMapping({"/online", "/online-users"})
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<UserDto>> getOnlineUsers() {
        return ApiResponse.ok(userService.getActiveUsers());
    }
}
