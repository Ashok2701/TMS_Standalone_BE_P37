package com.transport.tms.Pod.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PodCompleteRequestDTO {

    /** DELIVERED | PARTIAL | FAILED | REFUSED */
    private String status;

    private String recipientName;
    private String recipientRelation;

    /** data URL, e.g. "data:image/png;base64,...." */
    private String signatureBase64;

    /** data URLs, e.g. "data:image/jpeg;base64,...." */
    private List<String> photosBase64;

    private String remarks;

    /** Required when status is FAILED or REFUSED. */
    private String failureReason;

    private Double latitude;
    private Double longitude;
}
