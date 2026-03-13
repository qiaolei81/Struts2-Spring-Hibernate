package com.rml.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateMenuRequest {
    @NotBlank(message = "Menu name is required")
    private String name;
    private String url;
    private String iconClass;
    private String parentId;
    private int sequence;
}
