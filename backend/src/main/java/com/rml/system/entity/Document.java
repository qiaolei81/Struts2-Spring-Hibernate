package com.rml.system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "t_document")
public class Document extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "producer", length = 100)
    private String producer;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "manual_filename", length = 100)
    private String manualFilename;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
