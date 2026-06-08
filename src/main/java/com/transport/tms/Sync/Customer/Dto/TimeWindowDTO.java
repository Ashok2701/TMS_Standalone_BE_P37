package com.transport.tms.Sync.Customer.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TimeWindowDTO {

    private UUID id;

    private String fromTime;    // HH:MM

    private String toTime;      // HH:MM

    private Integer displayOrder;
}
