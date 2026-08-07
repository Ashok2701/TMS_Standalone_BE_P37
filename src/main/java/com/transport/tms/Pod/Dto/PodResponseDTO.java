package com.transport.tms.Pod.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class PodResponseDTO {

    private String podId;
    private String docNum;
    private String tripCode;
    private String driverId;
    private String status;
    private String recipientName;
    private String recipientRelation;
    private String signatureBase64;
    private List<String> photosBase64;
    private String remarks;
    private String failureReason;
    private Double latitude;
    private Double longitude;
    private LocalDateTime deliveredAt;
}
