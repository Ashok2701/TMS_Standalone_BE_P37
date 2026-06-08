package com.transport.tms.Fleet.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageVO {
    private String productCode;
    private byte[] image;
}
