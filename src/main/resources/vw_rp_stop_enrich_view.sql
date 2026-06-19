-- ============================================================
-- DATABASE  : PostgreSQL  (tms schema)
-- PURPOSE   : TMS Route Planner — Postgres enrichment for stops
--
-- THREE VIEWS:
--   1. vw_rp_dlv_enrich   — enrichment for DELIVERY stops (DLV)
--   2. vw_rp_pick_enrich  — enrichment for PICK TICKET stops (PICK)
--   3. vw_rp_stop_enrich  — UNION ALL of both (used by Spring)
--
-- JOIN KEY  : customer_code → matches X3 BPCODE
--             address_code  → matches X3 ADRESCODE
--             doc_type      → 'DLV' or 'PICK' (for filtering if needed)
-- ============================================================


-- ============================================================
-- VIEW 1 : tms.vw_rp_dlv_enrich
-- SOURCE  : XTMSDOCCONF WHERE XDOCTYP_0='SDN' AND LANNUM_0=5
--           → xr_document_config WHERE document_type = 'DLV'
-- ============================================================
DROP VIEW IF EXISTS tms.vw_rp_stop_enrich;
DROP VIEW IF EXISTS tms.vw_rp_dlv_enrich;
DROP VIEW IF EXISTS tms.vw_rp_pick_enrich;

CREATE VIEW tms.vw_rp_dlv_enrich AS
SELECT
    -- ── Doc type identifier ───────────────────────────────────
    'DLV'                       AS doc_type,

    -- ── Join keys ─────────────────────────────────────────────
    ca.customer_code,
    ca.address_code,

    -- ── Geo (p) → replaces GPS_X / GPS_Y from X3 ─────────────
    c.latitude,
    c.longitude,

    -- ── Service & waiting time (p) ────────────────────────────
    -- replaces A.XX10C_SERVT_0 (BPADDRESS in X3)
    c.service_time,
    -- replaces A.XWAITTIME_0 (BPADDRESS in X3)
    c.waiting_time,

    -- ── Time windows (p) ──────────────────────────────────────
    -- replaces BS.XFANYTIME / BS.XTANYTIME (BPDLVCUST in X3)
    ca.any_time_window,
    tw.from_time,
    tw.to_time,
    tw.display_order            AS time_window_order,

    -- ── TMS constraint flags ──────────────────────────────────
    ca.any_vehicle_category,
    ca.any_driver,

    -- ── Document config (p) ───────────────────────────────────
    -- replaces XTMSDOCCONF WHERE XDOCTYP_0='SDN' AND LANNUM_0=5
    -- display_name_en → ROUTETAG    (DC.XROUTAG_0)
    -- display_name_fr → ROUTETAGFRA (DC.XROUTAGFRA_0)
    -- color_code      → ROUTECOLOR  (DC.ROUTECOLOR)
    dc.display_name_en          AS route_tag,
    dc.display_name_fr          AS route_tag_fra,
    dc.color_code               AS route_color

FROM      tms.xr_customer_address            ca
JOIN      tms.xr_customer                    c
    ON    c.customer_code  = ca.customer_code
LEFT JOIN tms.xr_customer_address_timewindow tw
    ON    tw.address_code  = ca.address_code
    AND   tw.display_order = (
              SELECT MIN(tw2.display_order)
              FROM   tms.xr_customer_address_timewindow tw2
              WHERE  tw2.address_code = ca.address_code
          )
LEFT JOIN (
    SELECT display_name_en, display_name_fr, color_code
    FROM   tms.xr_document_config
    WHERE  document_type = 'DLV'
      AND  active = TRUE
    LIMIT  1
)                                            dc ON TRUE;


-- ============================================================
-- VIEW 2 : tms.vw_rp_pick_enrich
-- SOURCE  : XTMSDOCCONF WHERE XDOCTYP_0='BDP' AND LANNUM_0=9
--           → xr_document_config WHERE document_type = 'PICK'
-- ============================================================
CREATE VIEW tms.vw_rp_pick_enrich AS
SELECT
    -- ── Doc type identifier ───────────────────────────────────
    'PICK'                      AS doc_type,

    -- ── Join keys ─────────────────────────────────────────────
    ca.customer_code,
    ca.address_code,

    -- ── Geo (p) ───────────────────────────────────────────────
    c.latitude,
    c.longitude,

    -- ── Service & waiting time (p) ────────────────────────────
    c.service_time,
    c.waiting_time,

    -- ── Time windows (p) ──────────────────────────────────────
    ca.any_time_window,
    tw.from_time,
    tw.to_time,
    tw.display_order            AS time_window_order,

    -- ── TMS constraint flags ──────────────────────────────────
    ca.any_vehicle_category,
    ca.any_driver,

    -- ── Document config (p) ───────────────────────────────────
    -- replaces XTMSDOCCONF WHERE XDOCTYP_0='BDP' AND LANNUM_0=9
    dc.display_name_en          AS route_tag,
    dc.display_name_fr          AS route_tag_fra,
    dc.color_code               AS route_color

FROM      tms.xr_customer_address            ca
JOIN      tms.xr_customer                    c
    ON    c.customer_code  = ca.customer_code
LEFT JOIN tms.xr_customer_address_timewindow tw
    ON    tw.address_code  = ca.address_code
    AND   tw.display_order = (
              SELECT MIN(tw2.display_order)
              FROM   tms.xr_customer_address_timewindow tw2
              WHERE  tw2.address_code = ca.address_code
          )
LEFT JOIN (
    SELECT display_name_en, display_name_fr, color_code
    FROM   tms.xr_document_config
    WHERE  document_type = 'PICK'
      AND  active = TRUE
    LIMIT  1
)                                            dc ON TRUE;


-- ============================================================
-- VIEW 3 : tms.vw_rp_stop_enrich
-- UNION ALL of vw_rp_dlv_enrich + vw_rp_pick_enrich
-- This is the single view Spring queries.
-- Filter by doc_type = 'DLV' or 'PICK' as needed.
-- ============================================================
CREATE VIEW tms.vw_rp_stop_enrich AS
SELECT * FROM tms.vw_rp_dlv_enrich
UNION ALL
SELECT * FROM tms.vw_rp_pick_enrich;
