package com.transport.tms.Pod.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "xr_pod", schema = "tms")
@Getter
@Setter
public class ProofOfDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pod_id")
    private UUID podId;

    @Column(name = "doc_num", nullable = false, unique = true, length = 50)
    private String docNum;

    @Column(name = "trip_code", nullable = false, length = 60)
    private String tripCode;

    @Column(name = "driver_id", nullable = false, length = 30)
    private String driverId;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // DELIVERED | PARTIAL | FAILED | REFUSED

    @Column(name = "recipient_name", length = 150)
    private String recipientName;

    @Column(name = "recipient_relation", length = 100)
    private String recipientRelation;

    @Column(name = "signature", columnDefinition = "TEXT")
    private String signature; // base64 data URL

    @Column(name = "photos", columnDefinition = "TEXT")
    private String photosJson; // JSON array of base64 data URLs

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "failure_reason", length = 200)
    private String failureReason;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "delivered_at", nullable = false)
    private LocalDateTime deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (deliveredAt == null) deliveredAt = LocalDateTime.now();
    }
}
