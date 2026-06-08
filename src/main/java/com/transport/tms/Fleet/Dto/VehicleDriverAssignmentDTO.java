package com.transport.tms.Fleet.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class VehicleDriverAssignmentDTO {

    private UUID assignmentId;

    private String vehicleCode;
    private String vehicleName;

    private String driverId;
    private String driverName;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean active;

    private String remarks;
}