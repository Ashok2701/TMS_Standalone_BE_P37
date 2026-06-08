package com.transport.tms.Configuration.Document.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class DocumentConfigDTO {

    private UUID documentId;

    private String documentName;

    private String documentType;

    private String displayNameEn;

    private String displayNameFr;

    // Must be in #RRGGBB hex format e.g. "#6366F1"
    private String colorCode;

    private Boolean active;

    // Audit — sent by frontend on create/update
    private String createdBy;

    private String updatedBy;

    // Audit — returned by backend on GET
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
