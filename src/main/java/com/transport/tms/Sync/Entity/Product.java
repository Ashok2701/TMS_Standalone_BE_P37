package com.transport.tms.Sync.Entity;

import jakarta.persistence.Entity;
import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", unique = true)
    private String itemId;

    @Column(name = "item_name")
    private String itemName;

    private String status;

    @Column(name = "base_price")
    private Double basePrice;

    private String uom;

    private LocalDateTime when_modified;

    private LocalDateTime created_at;

    private LocalDateTime updated_at;
}