-- ============================================================
-- FIX: xr_customer_address composite PK
-- Root cause: BPAADD_0 ("10","001" etc) is NOT globally unique
-- in X3 — it repeats across customers. True unique key is
-- (customer_code, address_code).
--
-- Run this ONCE on existing databases.
-- Safe to skip if running master_ddl.sql fresh.
-- ============================================================

-- Step 1: Drop old single-column PK and any dependent FKs
ALTER TABLE tms.xr_customer_address_timewindow
    DROP CONSTRAINT IF EXISTS fk_addr_timewindow_addr;

ALTER TABLE tms.xr_customer_address_vehicle
    DROP CONSTRAINT IF EXISTS fk_addr_vehicle_addr;

ALTER TABLE tms.xr_customer_address_driver
    DROP CONSTRAINT IF EXISTS fk_addr_driver_addr;

ALTER TABLE tms.xr_customer_address
    DROP CONSTRAINT IF EXISTS pk_xr_customer_address;

-- Step 2: Add composite PK
ALTER TABLE tms.xr_customer_address
    ADD CONSTRAINT pk_xr_customer_address
    PRIMARY KEY (customer_code, address_code);

-- Step 3: Add customer_code FK column to child tables
ALTER TABLE tms.xr_customer_address_timewindow
    ADD COLUMN IF NOT EXISTS customer_code VARCHAR(50);

ALTER TABLE tms.xr_customer_address_vehicle
    ADD COLUMN IF NOT EXISTS customer_code VARCHAR(50);

ALTER TABLE tms.xr_customer_address_driver
    ADD COLUMN IF NOT EXISTS customer_code VARCHAR(50);

-- Step 4: Backfill customer_code in child tables from parent
UPDATE tms.xr_customer_address_timewindow tw
SET customer_code = a.customer_code
FROM tms.xr_customer_address a
WHERE tw.address_code = a.address_code
  AND tw.customer_code IS NULL;

UPDATE tms.xr_customer_address_vehicle v
SET customer_code = a.customer_code
FROM tms.xr_customer_address a
WHERE v.address_code = a.address_code
  AND v.customer_code IS NULL;

UPDATE tms.xr_customer_address_driver d
SET customer_code = a.customer_code
FROM tms.xr_customer_address a
WHERE d.address_code = a.address_code
  AND d.customer_code IS NULL;

-- Step 5: Restore FK constraints using composite key
ALTER TABLE tms.xr_customer_address_timewindow
    ADD CONSTRAINT fk_addr_timewindow_addr
    FOREIGN KEY (customer_code, address_code)
    REFERENCES tms.xr_customer_address (customer_code, address_code)
    ON DELETE CASCADE;

ALTER TABLE tms.xr_customer_address_vehicle
    ADD CONSTRAINT fk_addr_vehicle_addr
    FOREIGN KEY (customer_code, address_code)
    REFERENCES tms.xr_customer_address (customer_code, address_code)
    ON DELETE CASCADE;

ALTER TABLE tms.xr_customer_address_driver
    ADD CONSTRAINT fk_addr_driver_addr
    FOREIGN KEY (customer_code, address_code)
    REFERENCES tms.xr_customer_address (customer_code, address_code)
    ON DELETE CASCADE;

-- Step 6: Clear existing synced addresses so full re-sync picks up all 499
-- (Previously only ~21 were saved due to PK collision on "10", "001" etc)
TRUNCATE TABLE tms.xr_customer_address_timewindow;
TRUNCATE TABLE tms.xr_customer_address_vehicle;
TRUNCATE TABLE tms.xr_customer_address_driver;
DELETE FROM tms.xr_customer_address;

-- After running this script:
-- 1. Restart the backend
-- 2. Trigger CUSTOMER_ADDRESS sync from the dashboard
-- All 499 addresses should now sync correctly
