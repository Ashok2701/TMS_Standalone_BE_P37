-- ============================================================
-- MIGRATION: Move latitude/longitude from xr_customer
--            to xr_customer_address
-- ============================================================

-- 1. Add lat/lon columns to xr_customer_address
ALTER TABLE tms.xr_customer_address
    ADD COLUMN IF NOT EXISTS latitude    NUMERIC(10,7) NULL,
    ADD COLUMN IF NOT EXISTS longitude   NUMERIC(10,7) NULL;

-- 2. Also add service_time and waiting_time at address level
--    (previously only on xr_customer — now per address)
ALTER TABLE tms.xr_customer_address
    ADD COLUMN IF NOT EXISTS service_time  VARCHAR(10) NULL,
    ADD COLUMN IF NOT EXISTS waiting_time  VARCHAR(10) NULL;

-- 3. Copy existing lat/lon from xr_customer → all its addresses
--    (initial population — each address gets the customer's coords)
UPDATE tms.xr_customer_address ca
SET
    latitude      = c.latitude,
    longitude     = c.longitude,
    service_time  = c.service_time,
    waiting_time  = c.waiting_time
FROM tms.xr_customer c
WHERE c.customer_code = ca.customer_code
  AND c.latitude  IS NOT NULL
  AND c.longitude IS NOT NULL;

-- 4. Index for geo queries
CREATE INDEX IF NOT EXISTS idx_xr_cust_addr_latlon
    ON tms.xr_customer_address (latitude, longitude)
    WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

-- NOTE: Keep lat/lon on xr_customer as well for now (backward compat)
-- They can be removed later once all addresses are individually geocoded.
