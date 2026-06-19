package com.transport.tms.RoutePlanner.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RoutePlannerSiteDTO {

    private String siteCode;

    private String siteName;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Boolean tmsFlag;

    private String addressLine1;

    private String city;

    private String postalCode;

    private String countryCode;
}
