package com.transport.tms.Configuration.Document.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(
        name="xr_document_config",
        schema="tms"
)
@Getter
@Setter
public class DocumentConfig {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="document_id")
    private UUID documentId;


    @Column(name="document_name")
    private String documentName;


    @Column(name="document_type")
    private String documentType;


    @Column(name="display_name_en")
    private String displayNameEn;


    @Column(name="display_name_fr")
    private String displayNameFr;


    @Column(name="color_code")
    private String colorCode;


    @Column(name="active")
    private Boolean active = true;


    @Column(name="created_by")
    private String createdBy;


    @Column(name="created_at")
    private LocalDateTime createdAt;


    @Column(name="updated_by")
    private String updatedBy;


    @Column(name="updated_at")
    private LocalDateTime updatedAt;

}