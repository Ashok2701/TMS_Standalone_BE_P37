package com.transport.tms.Pod.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class PodTripListItemDTO {

    private String lvsNumber;
    private String tripNumber;
    private String driverId;
    private LocalDate date;
    private long documentCount;
}
