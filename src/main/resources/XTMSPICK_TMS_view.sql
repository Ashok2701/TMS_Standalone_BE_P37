USE [tbs]
GO

-- ============================================================
-- SERVER    : SQL Server  (tbs database)
-- SCHEMA    : LEWISB
-- VIEW      : LEWISB.XTMSPICK_TMS
-- PURPOSE   : TMS Route Planner — PICK TICKET stops
--             Same structure as LEWISB.XTMSDLVY_TMS.
--             Contains ONLY SQL Server (x) fields.
--
-- EXCLUDED (p — enriched from Postgres vw_rp_stop_enrich):
--             GPS_X, GPS_Y       → xr_customer.latitude / longitude
--             ROUTETAG           → xr_document_config.display_name_en
--             ROUTETAGFRA        → xr_document_config.display_name_fr
--             ROUTECOLOR         → xr_document_config.color_code
--             DOCSERTIME         → xr_document_config
--             SERVICETIME        → xr_customer.service_time
--             WaitingTime        → xr_customer.waiting_time
--             FROMTIME / TOTIME  → xr_customer_address_timewindow
--
-- REMOVED (r): NOOFPACKGS, SKILLSET, DLVFLG, AROUTECOCDESC,
--              APRODCATEGDESC, AVEHCLASSLISTDESC, DSCODE,
--              PRELISTCODE, SCHEDTYPE, PAIREDDOC, TRAILER,
--              LOADBAY, TAILGATE, PTLINK, PTHEADER, BPServiceTime,
--              VEHICLECLASSASSOC, StackHeight, Type, Timings,
--              Packing, Height, LoadingOrder, MISCPICK, Speciality,
--              ALLDRIVERS, ALLVEHCLASS, DRIVERLIST, DRIVERNAMELIST,
--              VEHCLASSLIST, VEHCLASSDESCLIST, PRIORITYORDER,
--              FROMTIME, TOTIME, availDays
--
-- DIFFERENCES vs XTMSDLVY_TMS:
--   Base table : STOPREH  (delivery uses SDELIVERY)
--   DOCTYPE    : 'PICK'   (delivery uses 'DLV')
--   ROUTECODEBGCLR : RC.XSTYLE_0  (delivery has '')
--   VEHICLEPLATE   : 'NOT_ALLOC'  (delivery uses S.TRLLICPLATE_0)
--   DLVMODE        : '9'           (delivery uses S.MDL_0)
--   Address prefix : BPD           (delivery uses BPI)
--   DOCINST        : ''            (no comment field on STOPREH)
--   DOCCONF join   : XDOCTYP_0='BDP' AND LANNUM_0=9
--                    (delivery uses 'SDN' AND LANNUM_0=5)
-- ============================================================

CREATE OR ALTER VIEW [LEWISB].[XTMSPICK_TMS]
AS
SELECT

    -- ── Site ──────────────────────────────────────────────────
    P.STOFCY_0                                      AS SITE,

    -- ── Priority ──────────────────────────────────────────────
    CASE
        WHEN P.XDLVPIO_0 <> 0 THEN
            CASE
                WHEN P.XDLVPIO_0 = 3 THEN 80
                WHEN P.XDLVPIO_0 = 2 THEN 40
                WHEN P.XDLVPIO_0 = 1 THEN 10
                ELSE 10
            END
        ELSE
            CASE
                WHEN BS.DLVPIO_0 = 3 THEN 80
                WHEN BS.DLVPIO_0 = 2 THEN 40
                WHEN BS.DLVPIO_0 = 1 THEN 10
                ELSE 10
            END
    END                                             AS PRIORITY,

    -- ── Document identity ─────────────────────────────────────
    P.PRHNUM_0                                      AS DOCNUM,
    'PICK'                                          AS DOCTYPE,
    'DROP'                                          AS MOVTYPE,

    -- ── Dates ─────────────────────────────────────────────────
    P.DLVDAT_0                                      AS DOCDATE,
    P.XODLVDAT_0                                    AS OGLDLVDATE,
    P.CPY_0                                         AS CPYCODE,

    -- ── Status ────────────────────────────────────────────────
    P.XDLV_STATUS_0                                 AS DLVYSTATUS,

    -- ── Route ─────────────────────────────────────────────────
    P.DRN_0                                         AS ROUTECODE,
    RC.LANMES_0                                     AS ROUTECODEDESC,
    RC.XSTYLE_0                                     AS ROUTECODEBGCLR,

    -- ── Business partner ──────────────────────────────────────
    P.BPCORD_0                                      AS BPCODE,
    B.BPRNAM_0                                      AS BPNAME,

    -- ── Address (BPD prefix — pick ticket specific) ───────────
    P.BPAADD_0                                      AS ADRESCODE,
    A.BPADES_0                                      AS ADRESNAME,
    P.BPDADDLIG_0                                   AS ADDLIG1,
    P.BPDADDLIG_1                                   AS ADDLIG2,
    P.BPDADDLIG_2                                   AS ADDLIG3,
    P.BPDPOSCOD_0                                   AS POSCODE,
    P.BPDCTY_0                                      AS CITY,
    P.BPDSAT_0                                      AS STATECODE,
    P.BPDCRY_0                                      AS COUNTRYCODE,
    P.BPDCRYNAM_0                                   AS COUNTRYNAME,

    -- ── Weight / Volume ───────────────────────────────────────
    P.PACNBR_0                                      AS NBPACK,
    P.NETWEI_0                                      AS NETWEIGHT,
    P.WEU_0                                         AS WEIGHTUNIT,
    P.VOL_0                                         AS VOLUME,
    P.VOU_0                                         AS VOLUME_UNIT,

    -- ── Driver / Vehicle ──────────────────────────────────────
    P.DRIVERID_0                                    AS DRIVERCODE,
    P.XX10C_LICPLA_0                                AS VEHICLECODE,
    CONVERT(NVARCHAR(10), 'NOT_ALLOC')
        COLLATE Latin1_General_BIN2                 AS VEHICLEPLATE,

    -- ── Trip / Route plan ─────────────────────────────────────
    P.XROUTNBR_0                                    AS TRIPNO,
    CONVERT(NVARCHAR(5), '9')
        COLLATE Latin1_General_BIN2                 AS DLVMODE,
    ISNULL(P.XX10C_NUMPC_0, '')                     AS VRCODE,
    ISNULL(
        SUBSTRING(D.XNUMPC_0, LEN(D.XNUMPC_0)-2, LEN(D.XNUMPC_0))
    , '')                                           AS VRSEQ,
    CASE
        WHEN P.XSEQUENCE_0 != 0 THEN P.XSEQUENCE_0
        ELSE ISNULL(D.SEQUENCE_0, 0)
    END                                             AS SEQ,

    -- ── Departure / Arrival ───────────────────────────────────
    P.DPEDAT_0                                      AS DEPDATE,
    P.ETD_0                                         AS DEPTIME,
    P.ARVDAT_0                                      AS ARVDATE,
    P.ETA_0                                         AS ARVTIME,

    -- ── Route status (same derived logic as XTMSDLVY_TMS) ────
    (CASE
        WHEN P.XX10C_NUMPC_0 IS NOT NULL OR P.XX10C_NUMPC_0 <> '' THEN
            CASE
                WHEN P.XDLV_STATUS_0 = 5
                                                    THEN 'Skipped'
                WHEN TP.lock = 0
                 AND TP.optistatus IN ('Open', 'open')
                                                    THEN 'Open'
                WHEN TP.lock = 0
                 AND TP.optistatus IN ('Optimized', 'optimized')
                                                    THEN 'Optimized'
                WHEN TP.lock = 1
                 AND (L.XVRSEL_0 IS NULL OR L.XVRSEL_0 = '')
                                                    THEN 'Locked'
                WHEN L.XVRSEL_0 IS NOT NULL
                 AND L.XLOADFLG_0 IN (1,2,3)       THEN 'LVS Generated'
                WHEN L.XVRSEL_0 IS NOT NULL
                 AND L.XLOADFLG_0 IN (4,6,7,8,9,10,12)
                                                    THEN 'LVS Confirmed'
                WHEN L.XVRSEL_0 IS NOT NULL
                 AND L.XLOADFLG_0 = 11             THEN 'Cancelled'
                WHEN L.XVRSEL_0 IS NOT NULL
                 AND L.XLOADFLG_0 = 5              THEN 'Completed'
                WHEN P.XX10C_NUMPC_0 = ''          THEN 'To Plan'
            END
     END)                                           AS ROUTESTATUS,

    -- ── Carrier ───────────────────────────────────────────────
    BC.BPTNAM_0                                     AS CARRIER,
    SS.STY_0                                        AS CARRCOLOR,

    -- ── Instructions ──────────────────────────────────────────
    ''                                              AS DOCINST

FROM       tbs.LEWISB.STOPREH       P
JOIN       tbs.LEWISB.BPARTNER      B
    ON     B.BPRNUM_0  = P.BPCORD_0
JOIN       tbs.LEWISB.BPADDRESS     A
    ON     A.BPANUM_0  = P.BPCORD_0
   AND     A.BPAADD_0  = P.BPAADD_0
   AND     A.BPATYP_0  = 1
LEFT JOIN  tbs.LEWISB.BPCARRIER     BC
    ON     BC.BPTNUM_0 = P.BPTNUM_0
JOIN       tbs.LEWISB.BPDLVCUST     BS
    ON     BS.BPCNUM_0 = P.BPCORD_0
   AND     BS.BPAADD_0 = P.BPAADD_0
LEFT JOIN  tbs.LEWISB.XX10CPLANCHD  D
    ON     D.SDHNUM_0  = P.PRHNUM_0
LEFT JOIN  tbs.LEWISB.ASTYLE        SS
    ON     SS.COD_0    = BC.XSTYLE_0
LEFT JOIN  tbs.LEWISB.XTMSROUTECODE RC
    ON     RC.LANNUM_0 = P.DRN_0
LEFT JOIN  tbs.LEWISB.XX10TRIPS     TP
    ON     TP.TRIPCODE = P.XX10C_NUMPC_0
LEFT JOIN  tbs.LEWISB.XX10CLODSTOH  L
    ON     L.XVRSEL_0  = TP.TRIPCODE

WHERE
    -- GPS validity gate (same as XTMSDLVY_TMS)
    P.XX10C_GEOX_0 IS NOT NULL
    AND P.XX10C_GEOX_0 <> ''
    AND P.XX10C_GEOY_0 IS NOT NULL
    AND P.XX10C_GEOY_0 <> ''
    -- Standard pick ticket validity
    AND P.XTYPE_0 <> 3;
GO
