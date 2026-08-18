-- xr_driver_session — replaces nothing, brand new table for the mobile
-- app's single-active-session login enforcement.
-- Matches Pod.Entity.DriverSession exactly.

CREATE TABLE tms.xr_driver_session (
    session_id     UUID PRIMARY KEY,
    driver_id      VARCHAR(30) NOT NULL,
    device_id      VARCHAR(200),
    device_model   VARCHAR(200),
    login_at       TIMESTAMP NOT NULL,
    logout_at      TIMESTAMP,
    active         BOOLEAN NOT NULL DEFAULT true
);

-- Speeds up the "does this driver already have an active session"
-- check performed on every login attempt.
CREATE INDEX idx_xr_driver_session_driver_active
    ON tms.xr_driver_session (driver_id, active);
