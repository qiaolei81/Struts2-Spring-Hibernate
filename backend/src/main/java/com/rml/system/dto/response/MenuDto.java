package com.rml.system.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class MenuDto {
    private String id;
    private String name;
    private String iconClass;
    private String url;
    private Integer sequence;
    private String parentId;
    private List<MenuDto> children;
}
