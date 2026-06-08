package com.transport.tms.Sync.Customer.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddressDriverDTO {

    private UUID id;

    private String driverId;
}
