package com.transport.tms.Sync.Dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerDTO {

    private String customerId;
    private String customerName;
    private String status;

    private String email;
    private String phone;

    private LocalDateTime whenModified;
}