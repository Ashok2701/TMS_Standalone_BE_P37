package com.transport.tms.Fleet.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostalCodeDetails {
    private String country;
    private String postalCode;
    private String city;
    private String subDivision;
}
