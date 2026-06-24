USE [tbs]
GO

-- ============================================================
-- SERVER  : SQL Server  (tbs database, LEWISB schema)
-- VIEW    : LEWISB.XTMSDLVY_LINES_TMS
-- PURPOSE : Product lines for TMS delivery stops (DROPS)
--           One row per product line per delivery document.
--           Joins SDELIVERYD (line) → ITMMASTER (product desc)
--           Filtered to same validity gate as XTMSDLVY_TMS.
-- USAGE   : WHERE DOCNUM = ? (single stop)
--        OR WHERE DOCNUM IN (?,?,…) (batch for a trip)
-- ============================================================

CREATE OR ALTER VIEW [LEWISB].[XTMSDLVY_LINES_TMS]
AS
SELECT

    -- ── Document identity ──────────────────────────────────
    L.SDHNUM_0                          AS DOCNUM,          -- matches XTMSDLVY_TMS.DOCNUM
    L.SDOLIG_0                          AS LINE_NUM,         -- line sequence
    L.CPY_0                             AS CPYCODE,

    -- ── Site ──────────────────────────────────────────────
    CASE
        WHEN S.SDHCAT_0 = 4 THEN S.STOFCY_0
        ELSE S.SALFCY_0
    END                                 AS SITE,

    -- ── Product ───────────────────────────────────────────
    L.ITMREF_0                          AS ITEM_CODE,        -- product code
    L.ITMDES1_0                         AS ITEM_DESC1,       -- description line 1
    L.ITMDES2_0                         AS ITEM_DESC2,       -- description line 2

    -- ── Quantities ────────────────────────────────────────
    L.QTYSTU_0                          AS QTY_ORDERED,      -- ordered in stock unit
    L.QTYDLV_0                          AS QTY_DELIVERED,    -- delivered qty
    L.STU_0                             AS STOCK_UNIT,       -- e.g. EA, KG, L
    L.PCU_0                             AS PACK_UNIT,        -- e.g. CS, PAL

    -- ── Weight / Volume per line ──────────────────────────
    L.NETWEI_0                          AS NET_WEIGHT,
    L.GROWEI_0                          AS GROSS_WEIGHT,
    L.VOL_0                             AS VOLUME,
    L.WEU_0                             AS WEIGHT_UNIT,
    L.VOU_0                             AS VOLUME_UNIT,

    -- ── Lot / Serial / Pack ───────────────────────────────
    L.LOT_0                             AS LOT,
    L.SER_0                             AS SERIAL_NO,
    L.PCUNUM_0                          AS PACK_NUM,         -- number of packs

    -- ── Line status ───────────────────────────────────────
    L.DMVSTA_0                          AS LINE_STATUS        -- 1=Active,2=Closed,3=Cancelled

FROM       tbs.LEWISB.SDELIVERYD    L
JOIN       tbs.LEWISB.SDELIVERY     S
    ON     S.SDHNUM_0  = L.SDHNUM_0

WHERE
    -- Same validity gate as XTMSDLVY_TMS header view
    S.XX10C_GEOX_0 IS NOT NULL
    AND S.XX10C_GEOX_0 <> ''
    AND S.XX10C_GEOY_0 IS NOT NULL
    AND S.XX10C_GEOY_0 <> ''
    AND NOT (
           S.XX10C_GEOX_0 LIKE '%N%'
        OR S.XX10C_GEOX_0 LIKE '%W%'
        OR S.XX10C_GEOX_0 LIKE '%º%'
    )
    AND S.XTYPE_0       <> 3
    AND (S.CFMFLG_0     <> 2 OR (S.XDLV_STATUS_0 NOT IN (8, 0)))
    AND S.X1C_PICKNUM_0  = ''
    AND S.XSPTSDH_0     <> 2
    -- Only active lines
    AND L.DMVSTA_0 NOT IN (3)   -- exclude cancelled lines
GO
