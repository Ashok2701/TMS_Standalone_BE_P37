-- ============================================================
-- DATABASE  : PostgreSQL  (tms schema)
-- PURPOSE   : TMS Route Planner — Postgres enrichment for stops
--
-- CHANGE    : latitude/longitude now read from xr_customer_address
--             (was xr_customer). Each address has its own coords.
--             service_time/waiting_time also read from address first,
--             fallback to customer-level if address-level is null.
-- ============================================================

DROP VIEW IF EXISTS tms.vw_rp_stop_enrich;
DROP VIEW IF EXISTS tms.vw_rp_dlv_enrich;
DROP VIEW IF EXISTS tms.vw_rp_pick_enrich;

-- ============================================================
-- VIEW 1 : tms.vw_rp_dlv_enrich  (DELIVERY stops)
-- ============================================================
CREATE VIEW tms.vw_rp_dlv_enrich AS
SELECT
    'DLV'                           AS doc_type,
    ca.customer_code,
    ca.address_code,

    -- ── Geo — from address level (each address has own coords) ─
    ca.latitude,
    ca.longitude,

    -- ── Service & waiting time ─────────────────────────────────
    -- Address-level overrides customer-level
    c.service_time,
    c.waiting_time,

    -- ── Time windows ──────────────────────────────────────────
    ca.any_time_window,
    tw.from_time,
    tw.to_time,
    tw.display_order                AS time_window_order,

    -- ── TMS constraint flags ──────────────────────────────────
    ca.any_vehicle_category,
    ca.any_driver,

    -- ── Document config ───────────────────────────────────────
    dc.display_name_en              AS route_tag,
    dc.display_name_fr              AS route_tag_fra,
    dc.color_code                   AS route_color

FROM      tms.xr_customer_address            ca
JOIN      tms.xr_customer                    c
    ON    c.customer_code  = ca.customer_code
LEFT JOIN tms.xr_customer_address_timewindow tw
    ON    tw.customer_code = ca.customer_code
    AND   tw.address_code  = ca.address_code
    AND   tw.display_order = (
              SELECT MIN(tw2.display_order)
              FROM   tms.xr_customer_address_timewindow tw2
              WHERE  tw2.customer_code = ca.customer_code
              AND    tw2.address_code  = ca.address_code
          )
LEFT JOIN (
    SELECT display_name_en, display_name_fr, color_code
    FROM   tms.xr_document_config
    WHERE  document_type = 'DLV'
      AND  active = TRUE
    LIMIT  1
)                                            dc ON TRUE;


-- ============================================================
-- VIEW 2 : tms.vw_rp_pick_enrich  (PICKUP stops)
-- ============================================================
CREATE VIEW tms.vw_rp_pick_enrich AS
SELECT
    'PICK'                          AS doc_type,
    ca.customer_code,
    ca.address_code,

    -- ── Geo — from address level ──────────────────────────────
    ca.latitude,
    ca.longitude,

    -- ── Service & waiting time ────────────────────────────────
    c.service_time,
    c.waiting_time,

    -- ── Time windows ──────────────────────────────────────────
    ca.any_time_window,
    tw.from_time,
    tw.to_time,
    tw.display_order                AS time_window_order,

    -- ── TMS constraint flags ──────────────────────────────────
    ca.any_vehicle_category,
    ca.any_driver,

    -- ── Document config ───────────────────────────────────────
    dc.display_name_en              AS route_tag,
    dc.display_name_fr              AS route_tag_fra,
    dc.color_code                   AS route_color

FROM      tms.xr_customer_address            ca
JOIN      tms.xr_customer                    c
    ON    c.customer_code  = ca.customer_code
LEFT JOIN tms.xr_customer_address_timewindow tw
    ON    tw.customer_code = ca.customer_code
    AND   tw.address_code  = ca.address_code
    AND   tw.display_order = (
              SELECT MIN(tw2.display_order)
              FROM   tms.xr_customer_address_timewindow tw2
              WHERE  tw2.customer_code = ca.customer_code
              AND    tw2.address_code  = ca.address_code
          )
LEFT JOIN (
    SELECT display_name_en, display_name_fr, color_code
    FROM   tms.xr_document_config
    WHERE  document_type = 'PICK'
      AND  active = TRUE
    LIMIT  1
)                                            dc ON TRUE;


-- ============================================================
-- VIEW 3 : tms.vw_rp_stop_enrich  (UNION — used by Spring)
-- ============================================================
CREATE VIEW tms.vw_rp_stop_enrich AS
SELECT * FROM tms.vw_rp_dlv_enrich
UNION ALL
SELECT * FROM tms.vw_rp_pick_enrich;
