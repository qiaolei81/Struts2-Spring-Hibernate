package com.rml.system.controller;

import com.rml.system.dto.ApiResponse;
import com.rml.system.dto.request.CreateAuthorityRequest;
import com.rml.system.dto.request.UpdateAuthorityRequest;
import com.rml.system.dto.response.AuthorityDto;
import com.rml.system.service.AuthorityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/authorities")
@RequiredArgsConstructor
public class AuthorityController {

    private final AuthorityService authorityService;

    @GetMapping("/tree")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<AuthorityDto>> getTree() {
        return ApiResponse.ok(authorityService.getTree());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_AUTH_LIST')")
    public ApiResponse<List<AuthorityDto>> listAll() {
        return ApiResponse.ok(authorityService.listAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_AUTH_ADD')")
    public ApiResponse<AuthorityDto> createAuthority(@Valid @RequestBody CreateAuthorityRequest req) {
        return ApiResponse.created(authorityService.createAuthority(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_AUTH_EDIT')")
    public ApiResponse<AuthorityDto> updateAuthority(@PathVariable String id,
                                                      @RequestBody UpdateAuthorityRequest req) {
        return ApiResponse.ok(authorityService.updateAuthority(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_AUTH_DELETE')")
    public ApiResponse<Void> deleteAuthority(@PathVariable String id) {
        authorityService.deleteAuthority(id);
        return ApiResponse.ok();
    }
}
