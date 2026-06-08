package com.transport.tms.Sync.Dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierDTO {

    private String vendorId;
    private String vendorName;
    private String status;

    private String email;
    private String phone;

    private LocalDateTime whenModified;
}