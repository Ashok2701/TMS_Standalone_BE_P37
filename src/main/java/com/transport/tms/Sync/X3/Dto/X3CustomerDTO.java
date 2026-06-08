package com.transport.tms.Sync.X3.Dto;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class X3CustomerDTO {


    private String customerCode;

    private String customerName;

    private String shortName;

    private String countryCode;

    private String currencyCode;

    private Boolean active;

}