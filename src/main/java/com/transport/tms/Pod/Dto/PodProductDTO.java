package com.transport.tms.Pod.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PodProductDTO {

    private String itemCode;
    private String description;
    private Double qtyOrdered;
    private Double netWeight;
    private String weightUnit;
    private Double volume;
    private String volumeUnit;
}
