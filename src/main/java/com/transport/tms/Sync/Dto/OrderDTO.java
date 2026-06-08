package com.transport.tms.Sync.Dto;

import lombok.Data;

@Data
public class OrderDTO {

    private Long recordNo;
    private String customerId;
    private Double amount;
    private String status;
}