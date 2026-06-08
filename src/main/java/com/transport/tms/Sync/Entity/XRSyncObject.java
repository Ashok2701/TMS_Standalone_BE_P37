package com.transport.tms.Sync.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "xr_sync_object")
@Getter
@Setter
public class XRSyncObject {


    @Id
    @Column(name = "object_code")
    private String objectCode;


    @Column(name = "object_name")
    private String objectName;


    @Column(name = "description")
    private String description;


    @Column(name = "active")
    private Boolean active;


    @Column(name = "sync_sequence")
    private Integer syncSequence;


    @Column(name = "x3_count")
    private Integer x3Count;


    @Column(name = "postgres_count")
    private Integer postgresCount;


    @Column(name = "difference_count")
    private Integer differenceCount;


    @Column(name = "status")
    private String status;


    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;


    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}