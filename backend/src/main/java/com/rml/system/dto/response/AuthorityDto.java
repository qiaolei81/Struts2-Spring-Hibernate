package com.rml.system.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AuthorityDto {
    private String id;
    private String name;
    private String description;
    private String url;
    private Integer sequence;
    private String parentId;
    private List<AuthorityDto> children;
}
