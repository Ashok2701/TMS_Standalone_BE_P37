package com.transport.tms.Configuration.Document.Dto;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class DocumentConfigDTO {


    private UUID documentId;


    private String documentName;


    private String documentType;


    private String displayNameEn;


    private String displayNameFr;


    private String colorCode;


    private Boolean active;


}