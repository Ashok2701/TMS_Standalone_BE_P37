package com.transport.tms.Pod.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PodStopSummaryDTO {

    private String docNum;
    private Integer seq;
    private String type;         // DROP | PICKUP
    private String docType;      // DLV | PICK
    private String clientName;
    private String bpCode;
    private String address;
    private String city;
    private String postalCity;
    private Double qty;
    private Double weight;
    private String weightUnit;
    /** PENDING if no POD submitted yet, otherwise the submitted POD's status. */
    private String status;
}
