package com.rml.system.controller;

import com.rml.system.dto.ApiResponse;
import com.rml.system.dto.request.AssignRolesRequest;
import com.rml.system.dto.request.CreateUserRequest;
import com.rml.system.dto.request.UpdateUserRequest;
import com.rml.system.dto.response.RoleStatDto;
import com.rml.system.dto.response.UserDto;
import com.rml.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_USER_LIST')")
    public ApiResponse<Page<UserDto>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(userService.listUsers(q, pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_USER_LIST')")
    public ApiResponse<List<UserDto>> listAll() {
        return ApiResponse.ok(userService.listAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_USER_ADD')")
    public ApiResponse<UserDto> createUser(@Valid @RequestBody CreateUserRequest req) {
        return ApiResponse.created(userService.createUser(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_USER_EDIT')")
    public ApiResponse<UserDto> updateUser(@PathVariable String id, @RequestBody UpdateUserRequest req) {
        return ApiResponse.ok(userService.updateUser(id, req));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_USER_DELETE')")
    public ApiResponse<Void> deleteUsers(@RequestParam String ids) {
        List<String> idList = Arrays.stream(ids.split(","))
            .map(String::trim).filter(s -> !s.isBlank()).collect(Collectors.toList());
        userService.deleteUsers(idList);
        return ApiResponse.ok();
    }

    @PutMapping("/roles")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_USER_ROLE_EDIT')")
    public ApiResponse<Void> assignRoles(@RequestBody AssignRolesRequest req) {
        userService.assignRoles(req);
        return ApiResponse.ok();
    }

    @GetMapping("/stats/by-role")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_USER_LIST')")
    public ApiResponse<List<RoleStatDto>> getRoleStats() {
        return ApiResponse.ok(userService.getRoleStats());
    }
}
