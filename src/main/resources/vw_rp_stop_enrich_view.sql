-- ============================================================
-- DATABASE  : PostgreSQL  (tms schema)
-- PURPOSE   : TMS Route Planner — Postgres enrichment for stops
--
-- JOIN KEY  : customer_code + address_code (composite)
--             latitude/longitude from xr_customer_address
--             service_time/waiting_time from xr_customer
--
-- NOTE ON DOC TYPE — two different things share the name "doc_type":
--   1. The 'doc_type' column emitted below ('DLV' / 'PICK') is only
--      the routing bucket used by the app to pick which X3 source
--      table to read (SDELIVERY vs STOPREH) and which of these two
--      views to union — it is NOT a real X3/xr_document_config code.
--   2. xr_document_config.document_type holds the REAL document type
--      codes (SDN = Sales Delivery, RTN = Sales Return, BDP = Pick
--      Ticket) used purely to look up display name / bgcolor.
--   These must never be conflated — the document_config join below
--   filters on 'SDN' / 'BDP' (2), not 'DLV' / 'PICK' (1).
--   'RTN' (Sales Return) currently has no stop source wired up to it
--   at all — deliveries and returns aren't distinguished upstream, so
--   every XTMSDLVY_TMS row resolves to 'SDN'. Add that distinction
--   (likely via SDELIVERY.SDHCAT_0) if Sales Returns need their own
--   tag/color on the map.
-- ============================================================

DROP VIEW IF EXISTS tms.vw_rp_stop_enrich;
DROP VIEW IF EXISTS tms.vw_rp_dlv_enrich;
DROP VIEW IF EXISTS tms.vw_rp_pick_enrich;

-- ============================================================
-- VIEW 1 : tms.vw_rp_dlv_enrich
-- ============================================================
CREATE OR REPLACE VIEW tms.vw_rp_dlv_enrich AS
SELECT
    'DLV'::text                     AS doc_type,
    ca.customer_code,
    ca.address_code,

    -- Geo: per address (customer_code + address_code match)
    ca.latitude,
    ca.longitude,

    -- Service/waiting: per customer
    c.service_time,
    c.waiting_time,

    -- Time windows
    ca.any_time_window,
    tw.from_time,
    tw.to_time,
    tw.display_order                AS time_window_order,

    -- Constraint flags
    ca.any_vehicle_category,
    ca.any_driver,

    -- Document config
    dc.display_name_en              AS route_tag,
    dc.display_name_fr              AS route_tag_fra,
    dc.color_code                   AS route_color

FROM tms.xr_customer_address ca
JOIN tms.xr_customer c
    ON  c.customer_code::text = ca.customer_code::text
LEFT JOIN tms.xr_customer_address_timewindow tw
    ON  tw.customer_code::text  = ca.customer_code::text
    AND tw.address_code::text   = ca.address_code::text
    AND tw.display_order = (
            SELECT MIN(tw2.display_order)
            FROM   tms.xr_customer_address_timewindow tw2
            WHERE  tw2.customer_code::text = ca.customer_code::text
            AND    tw2.address_code::text  = ca.address_code::text
        )
LEFT JOIN (
    SELECT display_name_en, display_name_fr, color_code
    FROM   tms.xr_document_config
    WHERE  document_type::text = 'SDN'   -- Sales Delivery (was 'DLV' — no row ever matched that)
      AND  active = TRUE
    LIMIT  1
) dc ON TRUE;


-- ============================================================
-- VIEW 2 : tms.vw_rp_pick_enrich
-- ============================================================
CREATE OR REPLACE VIEW tms.vw_rp_pick_enrich AS
SELECT
    'PICK'::text                    AS doc_type,
    ca.customer_code,
    ca.address_code,

    -- Geo: per address (customer_code + address_code match)
    ca.latitude,
    ca.longitude,

    -- Service/waiting: per customer
    c.service_time,
    c.waiting_time,

    -- Time windows
    ca.any_time_window,
    tw.from_time,
    tw.to_time,
    tw.display_order                AS time_window_order,

    -- Constraint flags
    ca.any_vehicle_category,
    ca.any_driver,

    -- Document config
    dc.display_name_en              AS route_tag,
    dc.display_name_fr              AS route_tag_fra,
    dc.color_code                   AS route_color

FROM tms.xr_customer_address ca
JOIN tms.xr_customer c
    ON  c.customer_code::text = ca.customer_code::text
LEFT JOIN tms.xr_customer_address_timewindow tw
    ON  tw.customer_code::text  = ca.customer_code::text
    AND tw.address_code::text   = ca.address_code::text
    AND tw.display_order = (
            SELECT MIN(tw2.display_order)
            FROM   tms.xr_customer_address_timewindow tw2
            WHERE  tw2.customer_code::text = ca.customer_code::text
            AND    tw2.address_code::text  = ca.address_code::text
        )
LEFT JOIN (
    SELECT display_name_en, display_name_fr, color_code
    FROM   tms.xr_document_config
    WHERE  document_type::text = 'BDP'   -- Pick Ticket (was 'PICK' — no row ever matched that)
      AND  active = TRUE
    LIMIT  1
) dc ON TRUE;


-- ============================================================
-- VIEW 3 : tms.vw_rp_stop_enrich  (used by Spring)
-- ============================================================
CREATE OR REPLACE VIEW tms.vw_rp_stop_enrich AS
SELECT * FROM tms.vw_rp_dlv_enrich
UNION ALL
SELECT * FROM tms.vw_rp_pick_enrich;
