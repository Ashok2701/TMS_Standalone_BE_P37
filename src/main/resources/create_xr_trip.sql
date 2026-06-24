-- ============================================================
-- Migration: create tms.xr_trip
-- Maps to SQL Server TMSNEW.XX10TRIPS
-- ============================================================

CREATE TABLE IF NOT EXISTS tms.xr_trip (

    -- PK
    trip_id         BIGSERIAL PRIMARY KEY,

    -- Identity
    trip_code       VARCHAR(60)     NOT NULL UNIQUE,   -- XVR-YYYYMMDD-SITE-N
    site            VARCHAR(10)     NOT NULL,
    doc_date        DATE            NOT NULL,

    -- Driver
    driver_id       VARCHAR(30),
    driver_name     VARCHAR(100)    NOT NULL,

    -- Vehicle
    vehicle_code    VARCHAR(20)     NOT NULL,

    -- Counts
    stops           INT             NOT NULL DEFAULT 0,
    drops           INT             NOT NULL DEFAULT 0,
    pickups         INT             NOT NULL DEFAULT 0,
    trips           INT             NOT NULL DEFAULT 1,
    no_of_packages  INT,

    -- Sites
    dep_site        VARCHAR(20),
    arr_site        VARCHAR(20),

    -- Time
    start_time      VARCHAR(20),
    end_time        VARCHAR(20),
    travel_time     VARCHAR(20),
    total_time      VARCHAR(20),
    service_time    VARCHAR(20),

    -- Weight / Volume
    total_weight    VARCHAR(50),
    total_volume    VARCHAR(50),
    capacity        VARCHAR(50),
    uom_capacity    VARCHAR(10),
    uom_volume      VARCHAR(10),
    uom_time        VARCHAR(10),
    uom_distance    VARCHAR(20),
    weight_pct      FLOAT,
    volume_pct      FLOAT,

    -- Distance / Cost
    total_distance  VARCHAR(20),
    fixed_cost      VARCHAR(20),
    distance_cost   VARCHAR(20),
    service_cost    VARCHAR(20),
    regular_cost    VARCHAR(100),
    overtime_cost   VARCHAR(100),
    total_cost      VARCHAR(20),

    -- Status / Control
    opti_status     VARCHAR(20)     DEFAULT 'Open',   -- Open | Optimised | Locked
    lock_flag       INT             DEFAULT 0,        -- 1 = locked/sent to X3
    force_seq       INT             DEFAULT 0,
    vr_seq          VARCHAR(4),
    start_index     INT,
    notes           VARCHAR(200),
    generated_by    VARCHAR(50),
    heu_exec        VARCHAR(7),
    dat_exec        TIMESTAMPTZ,

    -- JSONB objects (replaces nvarchar(max) from SQL Server)
    stop_objects    JSONB,           -- combined drops + pickups array
    vehicle_object  JSONB,           -- vehicle snapshot
    total_object    JSONB,           -- totals snapshot

    -- Doc capacity fields
    tot_capacity    VARCHAR(100),
    tot_volume_cap  VARCHAR(100),
    doc_capacity    VARCHAR(100),
    doc_volume      VARCHAR(100),
    per_capacity    FLOAT,
    per_volume      FLOAT,
    doc_qty         INT,
    uom_qty         VARCHAR(5),
    max_pallet_cnt  INT,
    job_id          VARCHAR(40),

    -- Alerts
    alert_flag      INT             DEFAULT 0,
    warning_notes   TEXT,
    appointment     INT             NOT NULL DEFAULT 0,
    freq_exist      INT             DEFAULT 0,
    po_processed    INT             DEFAULT 0,

    -- Audit
    user_code       VARCHAR(10)     NOT NULL DEFAULT 'SYSTEM',
    create_date     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_xr_trip_site_date   ON tms.xr_trip (site, doc_date);
CREATE INDEX IF NOT EXISTS idx_xr_trip_driver       ON tms.xr_trip (driver_id);
CREATE INDEX IF NOT EXISTS idx_xr_trip_vehicle      ON tms.xr_trip (vehicle_code);
CREATE INDEX IF NOT EXISTS idx_xr_trip_code         ON tms.xr_trip (trip_code);
CREATE INDEX IF NOT EXISTS idx_xr_trip_status       ON tms.xr_trip (opti_status);
CREATE INDEX IF NOT EXISTS idx_xr_trip_stop_objects ON tms.xr_trip USING gin (stop_objects);

-- Auto-update update_date
CREATE OR REPLACE FUNCTION tms.update_xr_trip_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_date = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_xr_trip_updated ON tms.xr_trip;
CREATE TRIGGER trg_xr_trip_updated
    BEFORE UPDATE ON tms.xr_trip
    FOR EACH ROW EXECUTE FUNCTION tms.update_xr_trip_timestamp();
