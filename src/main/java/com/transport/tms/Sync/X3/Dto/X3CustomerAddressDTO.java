package com.transport.tms.Sync.X3.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class X3CustomerAddressDTO {

    private String customerCode;

    private String addressCode;

    private String addressDescription;

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private String city;

    private String postalCode;

    private String stateCode;

    private String countryCode;

    private String countryName;

    private String phone;

    private String mobile;

    private String email;

    private String webSite;

    private Boolean defaultAddress;

}
