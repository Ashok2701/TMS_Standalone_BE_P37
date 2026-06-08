package com.transport.tms.Sync.Dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductDTO {

    private String itemId;
    private String itemName;
    private String status;
    private Double basePrice;
    private String uom;
    private LocalDateTime whenModified;
}