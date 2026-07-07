USE [tbs]
GO

-- ============================================================
-- VIEW: TMSNEW.XTMSDLVY_LINES_TMS
-- PURPOSE: Product lines for delivery stops (DROPS)
-- One row per product line per delivery document
-- ============================================================
CREATE OR ALTER VIEW [TMSNEW].[XTMSDLVY_LINES_TMS]
AS
SELECT
    -- Document identity
    L.SDHNUM_0                  AS DOCNUM,
    L.SDOLIG_0                  AS LINE_NUM,
    L.CPY_0                     AS CPYCODE,

    -- Product
    L.ITMREF_0                  AS ITEM_CODE,
    L.ITMDES1_0                 AS ITEM_DESC1,
    L.ITMDES2_0                 AS ITEM_DESC2,

    -- Quantities
    L.QTYPCU_0                  AS QTY_ORDERED,
    L.QTYSTU_0                  AS QTY_STOCK_UNIT,
    L.QTYDLV_0                  AS QTY_DELIVERED,
    L.STU_0                     AS STOCK_UNIT,
    L.PCU_0                     AS PACK_UNIT,

    -- Weight / Volume per line
    L.NETWEI_0                  AS NET_WEIGHT,
    L.GROWEI_0                  AS GROSS_WEIGHT,
    L.VOL_0                     AS VOLUME,
    L.WEU_0                     AS WEIGHT_UNIT,
    L.VOU_0                     AS VOLUME_UNIT,

    -- Lot / Serial / Packing
    L.LOT_0                     AS LOT,
    L.SER_0                     AS SERIAL,
    L.PCUNUM_0                  AS PACK_NUM,

    -- Site
    L.STOFCY_0                  AS SITE,

    -- Status
    L.DMVSTA_0                  AS LINE_STATUS

FROM tbs.TMSNEW.SDELIVERYD L

GO

-- ============================================================
-- VIEW: TMSNEW.XTMSPICK_LINES_TMS
-- PURPOSE: Product lines for pickup stops (PICKUPS)
-- ============================================================
CREATE OR ALTER VIEW [TMSNEW].[XTMSPICK_LINES_TMS]
AS
SELECT
    -- Document identity
    L.VCRNUM_0                  AS DOCNUM,
    L.VCRLIN_0                  AS LINE_NUM,
    L.CPY_0                     AS CPYCODE,

    -- Product
    L.ITMREF_0                  AS ITEM_CODE,
    L.ITMDES1_0                 AS ITEM_DESC1,
    L.ITMDES2_0                 AS ITEM_DESC2,

    -- Quantities
    L.QTYSTU_0                  AS QTY_ORDERED,
    L.QTYSTU_0                  AS QTY_STOCK_UNIT,
    L.QTYMVT_0                  AS QTY_DELIVERED,
    L.STU_0                     AS STOCK_UNIT,
    L.PCU_0                     AS PACK_UNIT,

    -- Weight / Volume per line
    L.NETWEI_0                  AS NET_WEIGHT,
    L.GROWEI_0                  AS GROSS_WEIGHT,
    L.VOLUME_0                  AS VOLUME,
    L.WEU_0                     AS WEIGHT_UNIT,
    L.VOU_0                     AS VOLUME_UNIT,

    -- Lot / Serial
    L.LOT_0                     AS LOT,
    L.SERIAL_0                  AS SERIAL,

    -- Site
    L.STOFCY_0                  AS SITE,

    -- Status
    L.STA_0                     AS LINE_STATUS

FROM tbs.TMSNEW.STOJOU L
WHERE L.MVTTYP_0 IN (2, 3)  -- picking movements

GO
