package com.transport.tms.Pod.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PodStopDetailDTO {

    private String docNum;
    private Integer seq;
    private String type;
    private String docType;
    private String clientName;
    private String bpCode;
    private String address;
    private String city;
    private String postalCity;
    private String arrivalTime;
    private String departureTime;
    private Double qty;
    private Double weight;
    private String weightUnit;
    private String status;
    private List<PodProductDTO> products;
}
