package com.rml.system.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AssignAuthoritiesRequest {
    private List<String> authorityIds;
}
