package com.transport.tms.Sync.Customer.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "xr_customer_address_timewindow", schema = "tms")
@Getter
@Setter
public class XRAddressTimeWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_code")
    private XRCustomerAddress address;

    @Column(name = "from_time", nullable = false)
    private String fromTime;    // HH:MM

    @Column(name = "to_time", nullable = false)
    private String toTime;      // HH:MM

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
