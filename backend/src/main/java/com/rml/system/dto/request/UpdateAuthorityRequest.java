package com.rml.system.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateAuthorityRequest {
    private String name;
    private String url;
    private String parentId;
    private String description;
    private int seq;
}
