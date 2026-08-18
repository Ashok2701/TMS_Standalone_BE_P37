package com.transport.tms.Pod.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One row per login attempt that actually succeeded — tracks whether a
 * driver is currently logged in on some device, so a second login on a
 * different device can be blocked with "Already logged in on another
 * device" instead of silently issuing a second valid token.
 */
@Entity
@Table(name = "xr_driver_session", schema = "tms")
@Getter
@Setter
public class DriverSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "driver_id", nullable = false, length = 30)
    private String driverId;

    @Column(name = "device_id", length = 200)
    private String deviceId;

    /** Free-text device model/OS string from the app, e.g. "Pixel 8 / Android 15". */
    @Column(name = "device_model", length = 200)
    private String deviceModel;

    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
