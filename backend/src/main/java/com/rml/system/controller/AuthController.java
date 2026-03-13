package com.rml.system.controller;

import com.rml.system.dto.ApiResponse;
import com.rml.system.dto.request.CreateUserRequest;
import com.rml.system.dto.request.LoginRequest;
import com.rml.system.dto.response.LoginResponse;
import com.rml.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = userService.authenticate(req.getUsername(), req.getPassword());
        return ApiResponse.ok(resp);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> register(@Valid @RequestBody CreateUserRequest req) {
        userService.createUser(req);
        return ApiResponse.ok();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok();
    }
}
