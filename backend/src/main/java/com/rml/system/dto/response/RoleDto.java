package com.rml.system.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class RoleDto {
    private String id;
    private String name;
    private String description;
    private List<AuthorityDto> authorities;
}
