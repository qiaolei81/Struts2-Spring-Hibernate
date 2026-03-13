package com.rml.system.controller;

import com.rml.system.dto.ApiResponse;
import com.rml.system.dto.request.AssignAuthoritiesRequest;
import com.rml.system.dto.request.CreateRoleRequest;
import com.rml.system.dto.request.UpdateRoleRequest;
import com.rml.system.dto.response.RoleDto;
import com.rml.system.service.RoleService;
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
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_ROLE_LIST')")
    public ApiResponse<Page<RoleDto>> listRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "name", required = false) String q) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(roleService.listRoles(q, pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_ROLE_LIST')")
    public ApiResponse<List<RoleDto>> listAll() {
        return ApiResponse.ok(roleService.listAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_ROLE_ADD')")
    public ApiResponse<RoleDto> createRole(@Valid @RequestBody CreateRoleRequest req) {
        return ApiResponse.created(roleService.createRole(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_ROLE_EDIT')")
    public ApiResponse<RoleDto> updateRole(@PathVariable String id, @RequestBody UpdateRoleRequest req) {
        return ApiResponse.ok(roleService.updateRole(id, req));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_ROLE_DELETE')")
    public ApiResponse<Void> deleteRoles(@RequestParam String ids) {
        List<String> idList = Arrays.stream(ids.split(","))
            .map(String::trim).filter(s -> !s.isBlank()).collect(Collectors.toList());
        roleService.deleteRoles(idList);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/authorities")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_ROLE_EDIT')")
    public ApiResponse<Void> assignAuthorities(@PathVariable String id,
                                                @RequestBody AssignAuthoritiesRequest req) {
        roleService.assignAuthorities(id, req);
        return ApiResponse.ok();
    }
}
