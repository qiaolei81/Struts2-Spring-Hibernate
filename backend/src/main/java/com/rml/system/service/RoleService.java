package com.rml.system.service;

import com.rml.system.dto.request.AssignAuthoritiesRequest;
import com.rml.system.dto.request.CreateRoleRequest;
import com.rml.system.dto.request.UpdateRoleRequest;
import com.rml.system.dto.response.AuthorityDto;
import com.rml.system.dto.response.RoleDto;
import com.rml.system.entity.Authority;
import com.rml.system.entity.Role;
import com.rml.system.exception.AppException;
import com.rml.system.repository.AuthorityRepository;
import com.rml.system.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;

    @Transactional(readOnly = true)
    public Page<RoleDto> listRoles(String q, Pageable pageable) {
        Page<Role> page = (q != null && !q.isBlank())
            ? roleRepository.findByNameContainingIgnoreCase(q, pageable)
            : roleRepository.findAll(pageable);
        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<RoleDto> listAll() {
        return roleRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public RoleDto createRole(CreateRoleRequest req) {
        Role role = new Role();
        role.setName(req.getName());
        role.setDescription(req.getDescription());
        if (req.getAuthorityIds() != null && !req.getAuthorityIds().isEmpty()) {
            role.setAuthorities(new HashSet<>(authorityRepository.findAllById(req.getAuthorityIds())));
        }
        return toDto(roleRepository.save(role));
    }

    @Transactional
    public RoleDto updateRole(String id, UpdateRoleRequest req) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Role not found: " + id));
        if (req.getName() != null) role.setName(req.getName());
        if (req.getDescription() != null) role.setDescription(req.getDescription());
        if (req.getAuthorityIds() != null) {
            role.setAuthorities(new HashSet<>(authorityRepository.findAllById(req.getAuthorityIds())));
        }
        return toDto(roleRepository.save(role));
    }

    @Transactional
    public void deleteRoles(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        roleRepository.deleteAllById(ids);
    }

    @Transactional
    public void assignAuthorities(String roleId, AssignAuthoritiesRequest req) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) return;
        if (req.getAuthorityIds() != null) {
            role.setAuthorities(new HashSet<>(authorityRepository.findAllById(req.getAuthorityIds())));
            roleRepository.save(role);
        }
    }

    private RoleDto toDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        if (role.getAuthorities() != null) {
            dto.setAuthorities(role.getAuthorities().stream().map(auth -> {
                AuthorityDto aDto = new AuthorityDto();
                aDto.setId(auth.getId());
                aDto.setName(auth.getName());
                aDto.setUrl(auth.getUrl());
                return aDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}
