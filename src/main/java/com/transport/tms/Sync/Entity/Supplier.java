package com.transport.tms.Sync.Entity;


import jakarta.persistence.*;
        import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Data
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, name = "vendor_id")
    private String vendorId;

    @Column(name = "vendor_name")
    private String vendorName;

    private String status;

    private String email;
    private String phone;

    private LocalDateTime when_modified;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}