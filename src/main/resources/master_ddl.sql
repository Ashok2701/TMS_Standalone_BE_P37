-- ============================================================
-- TMS STANDALONE — MASTER DDL
-- Run once in PostgreSQL under tms schema
-- Safe to re-run (uses IF NOT EXISTS / IF EXISTS guards)
-- ============================================================

-- ============================================================
-- SECTION 1: SYNC TABLES (created fresh from X3)
-- ============================================================

-- 1.1 xr_customer
CREATE TABLE IF NOT EXISTS tms.xr_customer (
    customer_code       VARCHAR(50)     NOT NULL,
    customer_name       VARCHAR(255),
    short_name          VARCHAR(100),
    country_code        VARCHAR(10),
    currency_code       VARCHAR(10),
    active              BOOLEAN         DEFAULT TRUE,
    synced_at           TIMESTAMP,

    -- TMS fields (never touched by sync)
    latitude            NUMERIC(11,7),
    longitude           NUMERIC(11,7),
    service_time        VARCHAR(5),                 -- HH:MM
    waiting_time        VARCHAR(5),                 -- HH:MM
    updated_by          VARCHAR(100),
    updated_at          TIMESTAMP,

    CONSTRAINT pk_xr_customer PRIMARY KEY (customer_code)
);


-- 1.2 xr_customer_address
CREATE TABLE IF NOT EXISTS tms.xr_customer_address (
    address_code            VARCHAR(50)     NOT NULL,
    customer_code           VARCHAR(50)     NOT NULL,
    address_description     VARCHAR(255),
    address_line1           VARCHAR(255),
    address_line2           VARCHAR(255),
    address_line3           VARCHAR(255),
    city                    VARCHAR(100),
    postal_code             VARCHAR(20),
    state_code              VARCHAR(50),
    country_code            VARCHAR(10),
    country_name            VARCHAR(100),
    phone                   VARCHAR(50),
    mobile                  VARCHAR(50),
    email                   VARCHAR(100),
    web_site                VARCHAR(255),
    default_address         BOOLEAN         DEFAULT FALSE,
    synced_at               TIMESTAMP,

    -- TMS flags (never touched by sync)
    any_time_window         BOOLEAN         DEFAULT FALSE,
    any_vehicle_category    BOOLEAN         DEFAULT FALSE,
    any_driver              BOOLEAN         DEFAULT FALSE,
    updated_by              VARCHAR(100),
    updated_at              TIMESTAMP,

    CONSTRAINT pk_xr_customer_address PRIMARY KEY (address_code)
);

CREATE INDEX IF NOT EXISTS idx_xr_cust_addr_cust_code
    ON tms.xr_customer_address (customer_code);


-- 1.3 xr_customer_address_timewindow  (TMS grid — delivery time windows)
CREATE TABLE IF NOT EXISTS tms.xr_customer_address_timewindow (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    address_code    VARCHAR(50) NOT NULL,
    from_time       VARCHAR(5)  NOT NULL,   -- HH:MM
    to_time         VARCHAR(5)  NOT NULL,   -- HH:MM
    display_order   INTEGER     DEFAULT 0,
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


-- 1.4 xr_customer_address_vehicle  (TMS grid — eligible vehicle categories)
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


-- 1.5 xr_customer_address_driver  (TMS grid — eligible drivers)
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


-- 1.6 xr_product
CREATE TABLE IF NOT EXISTS tms.xr_product (
    product_code        VARCHAR(50)     NOT NULL,
    product_name        VARCHAR(255),
    short_description   VARCHAR(255),
    product_category    VARCHAR(50),
    unit_of_measure     VARCHAR(20),
    sales_unit          VARCHAR(20),
    net_weight          NUMERIC(18,4),
    gross_weight        NUMERIC(18,4),
    volume              NUMERIC(18,4),
    weight_unit         VARCHAR(20),
    volume_unit         VARCHAR(20),
    active              BOOLEAN         DEFAULT TRUE,
    synced_at           TIMESTAMP,

    -- TMS fields (never touched by sync)
    service_time        VARCHAR(5),                 -- HH:MM
    updated_by          VARCHAR(100),
    updated_at          TIMESTAMP,

    CONSTRAINT pk_xr_product PRIMARY KEY (product_code)
);

CREATE INDEX IF NOT EXISTS idx_xr_product_category
    ON tms.xr_product (product_category);


-- ============================================================
-- SECTION 2: ALTER EXISTING TABLES
-- (run if tables were already created without TMS columns)
-- ============================================================

ALTER TABLE tms.xr_customer
    ADD COLUMN IF NOT EXISTS latitude       NUMERIC(11,7),
    ADD COLUMN IF NOT EXISTS longitude      NUMERIC(11,7),
    ADD COLUMN IF NOT EXISTS service_time   VARCHAR(5),
    ADD COLUMN IF NOT EXISTS waiting_time   VARCHAR(5),
    ADD COLUMN IF NOT EXISTS updated_by     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_at     TIMESTAMP;

ALTER TABLE tms.xr_customer_address
    ADD COLUMN IF NOT EXISTS any_time_window        BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS any_vehicle_category   BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS any_driver             BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS updated_by             VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_at             TIMESTAMP;

ALTER TABLE tms.xr_product
    ADD COLUMN IF NOT EXISTS service_time   VARCHAR(5),
    ADD COLUMN IF NOT EXISTS updated_by     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_at     TIMESTAMP;


-- ============================================================
-- SECTION 3: REGISTER SYNC OBJECTS IN DASHBOARD
-- ============================================================

INSERT INTO tms.xr_sync_object
    (object_code, object_name, description, active, sync_sequence,
     x3_count, postgres_count, difference_count, status,
     last_sync_time, created_at, updated_at)
VALUES
    ('CUSTOMER', 'Customer',
     'Syncs customer master data from X3',
     TRUE, 2, 0, 0, 0, 'PENDING', NULL, NOW(), NOW()),

    ('CUSTOMER_ADDRESS', 'Customer Address',
     'Syncs customer delivery/billing addresses from X3',
     TRUE, 3, 0, 0, 0, 'PENDING', NULL, NOW(), NOW()),

    ('PRODUCT', 'Product',
     'Syncs product master data from X3 item master',
     TRUE, 4, 0, 0, 0, 'PENDING', NULL, NOW(), NOW())

ON CONFLICT (object_code) DO NOTHING;


-- ============================================================
-- SUMMARY OF ALL TMS TABLES (for reference)
-- ============================================================
--
-- SYNCED FROM X3 (read-only in TMS UI):
--   tms.xr_site                          (existing)
--   tms.xr_customer                      (new)
--   tms.xr_customer_address              (new)
--   tms.xr_product                       (new)
--
-- TMS-ONLY CHILD GRIDS (managed via TMS UI, no X3 equivalent):
--   tms.xr_customer_address_timewindow   (delivery time windows per address)
--   tms.xr_customer_address_vehicle      (eligible vehicle categories per address)
--   tms.xr_customer_address_driver       (eligible drivers per address)
--
-- ============================================================
