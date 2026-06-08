package com.transport.tms.Sync.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name="xr_sync_history")
@Getter
@Setter
public class XRSyncHistory {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="sync_id")
    private UUID syncId;


    @Column(name="object_code")
    private String objectCode;


    @Column(name="started_at")
    private LocalDateTime startedAt;


    @Column(name="completed_at")
    private LocalDateTime completedAt;


    @Column(name="x3_count")
    private Integer x3Count;


    @Column(name="postgres_before_count")
    private Integer postgresBeforeCount;


    @Column(name="postgres_after_count")
    private Integer postgresAfterCount;


    @Column(name="inserted_count")
    private Integer insertedCount;


    @Column(name="updated_count")
    private Integer updatedCount;


    @Column(name="failed_count")
    private Integer failedCount;


    @Column(name="status")
    private String status;


    @Column(name="error_message")
    private String errorMessage;
}