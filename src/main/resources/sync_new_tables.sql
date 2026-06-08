-- ============================================================
-- TMS SYNC - NEW TABLES DDL
-- Run in PostgreSQL under tms schema
-- ============================================================

-- 1. xr_customer_address
CREATE TABLE IF NOT EXISTS tms.xr_customer_address (
    address_code        VARCHAR(50)  NOT NULL,
    customer_code       VARCHAR(50)  NOT NULL,
    address_description VARCHAR(255),
    address_line1       VARCHAR(255),
    address_line2       VARCHAR(255),
    address_line3       VARCHAR(255),
    city                VARCHAR(100),
    postal_code         VARCHAR(20),
    state_code          VARCHAR(50),
    country_code        VARCHAR(10),
    country_name        VARCHAR(100),
    phone               VARCHAR(50),
    mobile              VARCHAR(50),
    email               VARCHAR(100),
    web_site            VARCHAR(255),
    default_address     BOOLEAN      DEFAULT FALSE,
    synced_at           TIMESTAMP,
    CONSTRAINT pk_xr_customer_address PRIMARY KEY (address_code)
);

CREATE INDEX IF NOT EXISTS idx_xr_cust_addr_cust_code
    ON tms.xr_customer_address (customer_code);


-- 2. xr_product
CREATE TABLE IF NOT EXISTS tms.xr_product (
    product_code        VARCHAR(50)  NOT NULL,
    product_name        VARCHAR(255),
    short_description   VARCHAR(255),
    product_category    VARCHAR(50),
    unit_of_measure     VARCHAR(20),
    sales_unit          VARCHAR(20),
    net_weight          NUMERIC(18, 4),
    gross_weight        NUMERIC(18, 4),
    volume              NUMERIC(18, 4),
    weight_unit         VARCHAR(20),
    volume_unit         VARCHAR(20),
    active              BOOLEAN      DEFAULT TRUE,
    synced_at           TIMESTAMP,
    CONSTRAINT pk_xr_product PRIMARY KEY (product_code)
);

CREATE INDEX IF NOT EXISTS idx_xr_product_category
    ON tms.xr_product (product_category);


-- 3. Register new sync objects in dashboard table
INSERT INTO tms.xr_sync_object
    (object_code, object_name, description, active, sync_sequence,
     x3_count, postgres_count, difference_count, status,
     last_sync_time, created_at, updated_at)
VALUES
    ('CUSTOMER_ADDRESS', 'Customer Address',
     'Syncs customer delivery/billing addresses from X3',
     TRUE, 3, 0, 0, 0, 'PENDING', NULL, NOW(), NOW()),

    ('PRODUCT', 'Product',
     'Syncs product master data from X3 item master',
     TRUE, 4, 0, 0, 0, 'PENDING', NULL, NOW(), NOW())

ON CONFLICT (object_code) DO NOTHING;
