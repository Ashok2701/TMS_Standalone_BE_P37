package com.transport.tms.Fleet.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Document {
   private String docType;
   private String docNum;
   private String issuingAuthority;
   private Date issuingDate;
   private Date expiration;
   private Integer lineNum;
}
