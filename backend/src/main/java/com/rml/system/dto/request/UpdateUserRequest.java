package com.rml.system.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class UpdateUserRequest {
    private String username;
    private String password;
    private List<String> roleIds;
}
