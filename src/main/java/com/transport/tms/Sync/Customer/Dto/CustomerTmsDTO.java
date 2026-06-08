package com.transport.tms.Sync.Customer.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CustomerTmsDTO {

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String serviceTime;   // HH:MM

    private String waitingTime;   // HH:MM

    private String updatedBy;
}
