package com.transport.tms.Sync.Entity;

import jakarta.persistence.Entity;
import lombok.Data;

import jakarta.persistence.*;
        import java.time.LocalDateTime;

@Entity
@Table(name = "sync_metadata")
@Data
public class SyncMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", unique = true)
    private String entityName;   // products, customers, suppliers, orders

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

    @Column(name = "status")
    private String status;   // SUCCESS / FAILED

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}