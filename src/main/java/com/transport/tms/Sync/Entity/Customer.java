package com.transport.tms.Sync.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "customer_name")
    private String customerName;

    private String status;

    private String email;
    private String phone;

    private LocalDateTime when_modified;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}