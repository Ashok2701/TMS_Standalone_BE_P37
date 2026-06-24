USE [tbs]
GO

-- ============================================================
-- SERVER  : SQL Server  (tbs database, LEWISB schema)
-- VIEW    : LEWISB.XTMSDLVY_LINES_TMS
-- PURPOSE : Product lines for TMS delivery stops (DROPS)
--           One row per product line per delivery document.
--           Join SDELIVERYD (lines) → SDELIVERY (header validity)
-- USAGE   : WHERE DOCNUM = ?  OR  WHERE DOCNUM IN (?,?,…)
-- ============================================================

CREATE OR ALTER VIEW [LEWISB].[XTMSDLVY_LINES_TMS]
AS
SELECT
    L.SDHNUM_0          AS DOCNUM,
    L.SDOLIG_0          AS LINE_NUM,
    L.CPY_0             AS CPYCODE,
    CASE
        WHEN S.SDHCAT_0 = 4 THEN S.STOFCY_0
        ELSE S.SALFCY_0
    END                 AS SITE,
    L.ITMREF_0          AS ITEM_CODE,
    L.ITMDES1_0         AS ITEM_DESC1,
    L.ITMDES2_0         AS ITEM_DESC2,
    L.QTYSTU_0          AS QTY_ORDERED,
    L.QTYDLV_0          AS QTY_DELIVERED,
    L.STU_0             AS STOCK_UNIT,
    L.PCU_0             AS PACK_UNIT,
    L.PCUNUM_0          AS PACK_NUM,
    L.NETWEI_0          AS NET_WEIGHT,
    L.GROWEI_0          AS GROSS_WEIGHT,
    L.VOL_0             AS VOLUME,
    L.WEU_0             AS WEIGHT_UNIT,
    L.VOU_0             AS VOLUME_UNIT,
    L.LOT_0             AS LOT,
    L.SER_0             AS SERIAL_NO,
    L.DMVSTA_0          AS LINE_STATUS

FROM       tbs.LEWISB.SDELIVERYD    L
JOIN       tbs.LEWISB.SDELIVERY     S
    ON     S.SDHNUM_0  = L.SDHNUM_0

WHERE
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
    AND L.DMVSTA_0      <> 3
GO
