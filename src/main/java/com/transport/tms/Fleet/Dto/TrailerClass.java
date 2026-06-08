package com.transport.tms.Fleet.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TrailerClass {
    private String vehicleClass;
    private BigDecimal weight;
    private String weightUOM;
    private String weightUOMDesc;
    private BigDecimal volume;
    private String volumeUOM;
    private String volumeUOMDesc;
    private short nbAxle;
}
