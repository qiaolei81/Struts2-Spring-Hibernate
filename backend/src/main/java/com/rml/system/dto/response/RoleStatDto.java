package com.rml.system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoleStatDto {
    private String name;
    private long count;
}
