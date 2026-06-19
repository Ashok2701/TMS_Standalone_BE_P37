package com.transport.tms.RoutePlanner.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RoutePlannerRequestDTO {

    private String siteCode;

    private LocalDate planDate;
}
