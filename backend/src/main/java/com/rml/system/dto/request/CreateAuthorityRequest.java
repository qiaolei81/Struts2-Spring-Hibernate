package com.rml.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateAuthorityRequest {
    @NotBlank(message = "Authority name is required")
    private String name;
    private String url;
    private String parentId;
    private String description;
    private int seq;
}
