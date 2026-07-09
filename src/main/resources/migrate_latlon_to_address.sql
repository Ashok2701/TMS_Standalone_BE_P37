-- ============================================================
-- MIGRATION: Move latitude/longitude from xr_customer
--            to xr_customer_address
-- ============================================================

-- 1. Add lat/lon columns to xr_customer_address
ALTER TABLE tms.xr_customer_address
    ADD COLUMN IF NOT EXISTS latitude    NUMERIC(10,7) NULL,
    ADD COLUMN IF NOT EXISTS longitude   NUMERIC(10,7) NULL;

-- 3. Copy existing lat/lon from xr_customer → all its addresses
--    (initial population — each address gets the customer's coords)
UPDATE tms.xr_customer_address ca
SET
    latitude      = c.latitude,
    longitude     = c.longitude
FROM tms.xr_customer c
WHERE c.customer_code = ca.customer_code
  AND c.latitude  IS NOT NULL
  AND c.longitude IS NOT NULL
  AND (ca.latitude IS NULL OR ca.longitude IS NULL);

-- 4. Index for geo queries
CREATE INDEX IF NOT EXISTS idx_xr_cust_addr_latlon
    ON tms.xr_customer_address (latitude, longitude)
    WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

-- NOTE: Keep lat/lon on xr_customer as well for now (backward compat)
-- They can be removed later once all addresses are individually geocoded.

-- ============================================================
-- Add active column to xr_customer_address and xr_site
-- (for soft-delete when record removed from X3)
-- ============================================================
ALTER TABLE tms.xr_customer_address
    ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;

ALTER TABLE tms.xr_site
    ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;

-- Set all existing records as active
UPDATE tms.xr_customer_address SET active = TRUE WHERE active IS NULL;
UPDATE tms.xr_site              SET active = TRUE WHERE active IS NULL;

-- ============================================================
-- Add site column to xr_vehicle
-- ============================================================
ALTER TABLE tms.xr_vehicle
    ADD COLUMN IF NOT EXISTS site VARCHAR(20) NULL;

-- Optional: index for filtering vehicles by site
CREATE INDEX IF NOT EXISTS idx_xr_vehicle_site ON tms.xr_vehicle (site);

COMMENT ON COLUMN tms.xr_vehicle.site IS 'Site/facility code — FK to xr_site.site_code';
