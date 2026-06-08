-- ============================================================
-- TMS SPECIFIC FIELDS DDL
-- Run in PostgreSQL under tms schema
-- ============================================================

-- ── 1. xr_customer — add TMS columns ────────────────────────
ALTER TABLE tms.xr_customer
    ADD COLUMN IF NOT EXISTS latitude       NUMERIC(11,7),
    ADD COLUMN IF NOT EXISTS longitude      NUMERIC(11,7),
    ADD COLUMN IF NOT EXISTS service_time   VARCHAR(5),   -- HH:MM
    ADD COLUMN IF NOT EXISTS waiting_time   VARCHAR(5),   -- HH:MM
    ADD COLUMN IF NOT EXISTS updated_by     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_at     TIMESTAMP;


-- ── 2. xr_customer_address — add TMS flag columns ───────────
ALTER TABLE tms.xr_customer_address
    ADD COLUMN IF NOT EXISTS any_time_window        BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS any_vehicle_category   BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS any_driver             BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS updated_by             VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_at             TIMESTAMP;


-- ── 3. xr_customer_address_timewindow ───────────────────────
CREATE TABLE IF NOT EXISTS tms.xr_customer_address_timewindow (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    address_code    VARCHAR(50)     NOT NULL,
    from_time       VARCHAR(5)      NOT NULL,   -- HH:MM
    to_time         VARCHAR(5)      NOT NULL,   -- HH:MM
    display_order   INTEGER         DEFAULT 0,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    CONSTRAINT pk_addr_timewindow PRIMARY KEY (id),
    CONSTRAINT fk_addr_timewindow_addr
        FOREIGN KEY (address_code)
        REFERENCES tms.xr_customer_address (address_code)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_addr_timewindow_addr
    ON tms.xr_customer_address_timewindow (address_code);


-- ── 4. xr_customer_address_vehicle ──────────────────────────
CREATE TABLE IF NOT EXISTS tms.xr_customer_address_vehicle (
    id                      UUID        NOT NULL DEFAULT gen_random_uuid(),
    address_code            VARCHAR(50) NOT NULL,
    vehicle_category_code   VARCHAR(50) NOT NULL,
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    CONSTRAINT pk_addr_vehicle PRIMARY KEY (id),
    CONSTRAINT uq_addr_vehicle UNIQUE (address_code, vehicle_category_code),
    CONSTRAINT fk_addr_vehicle_addr
        FOREIGN KEY (address_code)
        REFERENCES tms.xr_customer_address (address_code)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_addr_vehicle_addr
    ON tms.xr_customer_address_vehicle (address_code);


-- ── 5. xr_customer_address_driver ───────────────────────────
CREATE TABLE IF NOT EXISTS tms.xr_customer_address_driver (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    address_code    VARCHAR(50) NOT NULL,
    driver_id       VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    CONSTRAINT pk_addr_driver PRIMARY KEY (id),
    CONSTRAINT uq_addr_driver UNIQUE (address_code, driver_id),
    CONSTRAINT fk_addr_driver_addr
        FOREIGN KEY (address_code)
        REFERENCES tms.xr_customer_address (address_code)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_addr_driver_addr
    ON tms.xr_customer_address_driver (address_code);


-- ── 6. xr_product — add TMS column ──────────────────────────
ALTER TABLE tms.xr_product
    ADD COLUMN IF NOT EXISTS service_time   VARCHAR(5),   -- HH:MM
    ADD COLUMN IF NOT EXISTS updated_by     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_at     TIMESTAMP;

