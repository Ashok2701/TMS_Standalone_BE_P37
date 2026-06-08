package com.transport.tms.Fleet.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "xr_vehicle_driver_assignment")
@Getter
@Setter
public class VehicleDriverAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "assignment_id")
    private UUID assignmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_code")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}