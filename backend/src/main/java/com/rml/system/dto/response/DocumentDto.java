package com.rml.system.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentDto {
    private String id;
    private String model;
    private String name;
    private String producer;
    private Integer quantity;
    private String manualFilename;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
