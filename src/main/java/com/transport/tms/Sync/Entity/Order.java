package com.transport.tms.Sync.Entity;

import jakarta.persistence.*;
        import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, name = "record_no")
    private Long recordNo;

    private String customer_id;

    private Double amount;
    private String status;

    private LocalDateTime when_modified;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
