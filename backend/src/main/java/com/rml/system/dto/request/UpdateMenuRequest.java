package com.rml.system.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateMenuRequest {
    private String name;
    private String url;
    private String iconClass;
    private String parentId;
    private int sequence;
}
