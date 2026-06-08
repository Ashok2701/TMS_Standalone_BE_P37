package com.transport.tms.Sync.X3.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class X3ProductDTO {

    private String productCode;

    private String productName;

    private String shortDescription;

    private String productCategory;

    private String unitOfMeasure;

    private String salesUnit;

    private Double netWeight;

    private Double grossWeight;

    private Double volume;

    private String weightUnit;

    private String volumeUnit;

    private Boolean active;

}
