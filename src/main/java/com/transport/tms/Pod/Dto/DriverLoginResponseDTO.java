package com.transport.tms.Pod.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DriverLoginResponseDTO {

    private String accessToken;

    private String driverId;

    private String driverName;

    private String username;

    private String site;

    private String mobileNo;
}
