package com.rml.system.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class UpdateRoleRequest {
    private String name;
    private String description;
    private List<String> authorityIds;
}
