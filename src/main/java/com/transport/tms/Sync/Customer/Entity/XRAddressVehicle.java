package com.transport.tms.Sync.Customer.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "xr_customer_address_vehicle", schema = "tms")
@Getter
@Setter
public class XRAddressVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_code")
    private XRCustomerAddress address;

    @Column(name = "vehicle_category_code", nullable = false)
    private String vehicleCategoryCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
