package com.rml.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateEquipmentRequest {
    @NotBlank(message = "Model is required")
    private String model;
    private String name;
    private String producer;
    private int quantity;
    private String description;
}
