package com.rml.system.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateDocumentRequest {
    private String model;
    private String name;
    private String producer;
    private int quantity;
    private String description;
}
