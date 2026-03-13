package com.rml.system.service;

import com.rml.system.dto.request.CreateAuthorityRequest;
import com.rml.system.dto.request.UpdateAuthorityRequest;
import com.rml.system.dto.response.AuthorityDto;
import com.rml.system.entity.Authority;
import com.rml.system.exception.AppException;
import com.rml.system.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final AuthorityRepository authorityRepository;

    @Transactional(readOnly = true)
    public List<AuthorityDto> getTree() {
        List<Authority> roots = authorityRepository.findByParentIsNullOrderBySequenceAsc();
        return roots.stream().map(this::toDtoWithChildren).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuthorityDto> listAll() {
        return authorityRepository.findAll().stream().map(this::toFlatDto).collect(Collectors.toList());
    }

    @Transactional
    public AuthorityDto createAuthority(CreateAuthorityRequest req) {
        Authority authority = new Authority();
        authority.setName(req.getName());
        authority.setUrl(req.getUrl());
        authority.setDescription(req.getDescription());
        authority.setSequence(req.getSeq());
        if (req.getParentId() != null && !req.getParentId().isBlank()) {
            Authority parent = authorityRepository.findById(req.getParentId())
                .orElseThrow(() -> AppException.notFound("Parent authority not found"));
            authority.setParent(parent);
        }
        return toFlatDto(authorityRepository.save(authority));
    }

    @Transactional
    public AuthorityDto updateAuthority(String id, UpdateAuthorityRequest req) {
        Authority authority = authorityRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Authority not found: " + id));
        if (req.getName() != null) authority.setName(req.getName());
        if (req.getUrl() != null) authority.setUrl(req.getUrl());
        if (req.getDescription() != null) authority.setDescription(req.getDescription());
        authority.setSequence(req.getSeq());
        if (req.getParentId() != null && !req.getParentId().isBlank()) {
            Authority parent = authorityRepository.findById(req.getParentId())
                .orElseThrow(() -> AppException.notFound("Parent authority not found"));
            authority.setParent(parent);
        } else if (req.getParentId() != null && req.getParentId().isBlank()) {
            authority.setParent(null);
        }
        return toFlatDto(authorityRepository.save(authority));
    }

    @Transactional
    public void deleteAuthority(String id) {
        authorityRepository.deleteById(id);
    }

    private AuthorityDto toDtoWithChildren(Authority auth) {
        AuthorityDto dto = toFlatDto(auth);
        dto.setChildren(auth.getChildren().stream()
            .map(this::toDtoWithChildren)
            .collect(Collectors.toList()));
        return dto;
    }

    private AuthorityDto toFlatDto(Authority auth) {
        AuthorityDto dto = new AuthorityDto();
        dto.setId(auth.getId());
        dto.setName(auth.getName());
        dto.setDescription(auth.getDescription());
        dto.setUrl(auth.getUrl());
        dto.setSequence(auth.getSequence());
        dto.setParentId(auth.getParent() != null ? auth.getParent().getId() : null);
        return dto;
    }
}
