USE [tbs]
GO

-- ============================================================
-- SERVER    : SQL Server  (tbs database)
-- VIEW      : TMSNEW.XTMSDLVY_TMS
-- PURPOSE   : TMS Route Planner — DELIVERY stops (DROPS)
--             Standalone version. Contains ONLY fields sourced
--             from X3 (marked x in field classification).
--
-- EXCLUDED  : GPS_X, GPS_Y         → come from Postgres xr_customer
--             ROUTETAG, ROUTECOLOR,
--             ROUTETAGFRA,DOCSERTIME,
--             SERVICETIME,WaitingTime → come from Postgres xr_document_config
--                                       / xr_customer
--
-- REMOVED   : NOOFPACKGS, SKILLSET, DLVFLG, AROUTECOCDESC,
--             APRODCATEGDESC, AVEHCLASSLISTDESC, DSCODE,
--             PRELISTCODE, SCHEDTYPE, PAIREDDOC, TRAILER,
--             LOADBAY, TAILGATE, PTLINK, PTHEADER, BPServiceTime,
--             VEHICLECLASSASSOC, StackHeight, Type, Timings,
--             Packing, Height, LoadingOrder, MISCPICK, Speciality,
--             ALLDRIVERS, ALLVEHCLASS, DRIVERNAMELIST, DRIVERLIST,
--             VEHCLASSLIST, VEHCLASSDESCLIST, PRIORITYORDER,
--             FROMTIME, TOTIME, availDays
--
-- FILTER    : SITE + DOCDATE passed as WHERE params from Spring.
--             GPS validity WHERE kept here as stop-validity gate
--             (X3 must have coords for stop to be valid).
-- ============================================================

CREATE OR ALTER VIEW [TMSNEW].[XTMSDLVY_TMS]
AS
SELECT

    -- ── Site ─────────────────────────────────────────────────
    CASE
        WHEN S.SDHCAT_0 = 4 THEN S.STOFCY_0
        ELSE S.SALFCY_0
    END                                             AS SITE,

    -- ── Priority (from BPDLVCUST) ─────────────────────────────
    CASE
        WHEN BS.DLVPIO_0 = 2 THEN 80
        WHEN BS.DLVPIO_0 = 1 THEN 10
        ELSE 10
    END                                             AS PRIORITY,

    -- ── Document identity ─────────────────────────────────────
    S.SDHNUM_0                                      AS DOCNUM,
    'DLV'                                           AS DOCTYPE,
    'DROP'                                          AS MOVTYPE,

    -- ── Dates ─────────────────────────────────────────────────
    S.DLVDAT_0                                      AS DOCDATE,
    S.XODLVDAT_0                                    AS OGLDLVDATE,
    S.CPY_0                                         AS CPYCODE,

    -- ── Delivery status ───────────────────────────────────────
    S.XDLV_STATUS_0                                 AS DLVYSTATUS,

    -- ── Route ─────────────────────────────────────────────────
    S.DRN_0                                         AS ROUTECODE,
    RC.LANMES_0                                     AS ROUTECODEDESC,
    ''                                              AS ROUTECODEBGCLR,

    -- ── Business partner ──────────────────────────────────────
    S.BPCORD_0                                      AS BPCODE,
    B.BPRNAM_0                                      AS BPNAME,

    -- ── Address ───────────────────────────────────────────────
    S.BPAADD_0                                      AS ADRESCODE,
    A.BPADES_0                                      AS ADRESNAME,
    S.BPIADDLIG_0                                   AS ADDLIG1,
    S.BPIADDLIG_1                                   AS ADDLIG2,
    S.BPIADDLIG_2                                   AS ADDLIG3,
    S.BPIPOSCOD_0                                   AS POSCODE,
    S.BPICTY_0                                      AS CITY,
    S.BPISAT_0                                      AS STATECODE,
    S.BPICRY_0                                      AS COUNTRYCODE,
    S.BPICRYNAM_0                                   AS COUNTRYNAME,

    -- ── Weight / Volume ───────────────────────────────────────
    S.PACNBR_0                                      AS NBPACK,
    S.NETWEI_0                                      AS NETWEIGHT,
    S.WEU_0                                         AS WEIGHTUNIT,
    S.VOL_0                                         AS VOLUME,
    S.VOU_0                                         AS VOLUME_UNIT,

    -- ── Driver / Vehicle ──────────────────────────────────────
    S.DRIVERID_0                                    AS DRIVERCODE,
    S.LICPLATE_0                                    AS VEHICLECODE,
    S.TRLLICPLATE_0                                 AS VEHICLEPLATE,

    -- ── Trip / Route plan ─────────────────────────────────────
    S.XROUTNBR_0                                    AS TRIPNO,
    IIF(LEN(S.MDL_0) = 0, '0', S.MDL_0)            AS DLVMODE,
    ISNULL(S.XX10C_NUMPC_0, '')                     AS VRCODE,
    ISNULL(
        SUBSTRING(S.XX10C_NUMPC_0, LEN(S.XX10C_NUMPC_0)-2, LEN(S.XX10C_NUMPC_0))
    , '')                                           AS VRSEQ,
    ISNULL(S.XSEQUENCE_0, 0)                        AS SEQ,

    -- ── Departure / Arrival ───────────────────────────────────
    S.DPEDAT_0                                      AS DEPDATE,
    S.ETD_0                                         AS DEPTIME,
    S.ARVDAT_0                                      AS ARVDATE,
    S.ETA_0                                         AS ARVTIME,

    -- ── Route status — derived from SDELIVERY alone (x3 stops
    -- table only; no join to XX10CPLANCHD/XX10CLODSTOH/XX10TRIPS,
    -- which are trip-planning state tables, not stops tables, and
    -- were the source of duplicate-row fan-out) ────────────────
    (CASE
        WHEN S.XX10C_NUMPC_0 IS NULL OR S.XX10C_NUMPC_0 = ''
                                                    THEN 'To Plan'
        WHEN S.XDLV_STATUS_0 = 5                   THEN 'Skipped'
        WHEN S.XDLV_STATUS_0 = 8                   THEN 'Released'
        WHEN S.XDLV_STATUS_0 = 2                   THEN 'Locked'
        WHEN S.XDLV_STATUS_0 = 1                   THEN 'Allocated'
        ELSE                                             'Open'
    END)                                            AS ROUTESTATUS,

    -- ── Carrier ───────────────────────────────────────────────
    BC.BPTNAM_0                                     AS CARRIER,
    SS.STY_0                                        AS CARRCOLOR,

    -- ── Instructions ──────────────────────────────────────────
    S.XCOMMENT_0                                    AS DOCINST

FROM       tbs.TMSNEW.SDELIVERY     S
JOIN       tbs.TMSNEW.BPARTNER      B
    ON     B.BPRNUM_0  = S.BPCORD_0
JOIN       tbs.TMSNEW.BPADDRESS     A
    ON     A.BPANUM_0  = S.BPCORD_0
   AND     A.BPAADD_0  = S.BPAADD_0
   AND     A.BPATYP_0  = 1
LEFT JOIN  tbs.TMSNEW.BPCARRIER     BC
    ON     BC.BPTNUM_0 = S.BPTNUM_0
JOIN       tbs.TMSNEW.BPDLVCUST     BS
    ON     BS.BPCNUM_0 = S.BPCORD_0
   AND     BS.BPAADD_0 = S.BPAADD_0
LEFT JOIN  tbs.TMSNEW.ASTYLE        SS
    ON     SS.COD_0    = BC.XSTYLE_0
LEFT JOIN  tbs.TMSNEW.XTMSROUTECODE RC
    ON     RC.LANNUM_0 = S.DRN_0

WHERE
    -- GPS must exist in X3 as stop-validity gate
    -- (actual lat/lon served from Postgres xr_customer)
    S.XX10C_GEOX_0 IS NOT NULL
    AND S.XX10C_GEOX_0 <> ''
    AND S.XX10C_GEOY_0 IS NOT NULL
    AND S.XX10C_GEOY_0 <> ''
    AND NOT (
           S.XX10C_GEOX_0 LIKE '%N%'
        OR S.XX10C_GEOX_0 LIKE '%W%'
        OR S.XX10C_GEOX_0 LIKE '%º%'
    )
    -- Standard delivery validity (same as original XTMSDLVY)
    AND S.XTYPE_0       <> 3
    AND (S.CFMFLG_0     <> 2 OR (S.XDLV_STATUS_0 NOT IN (8, 0)))
    AND S.X1C_PICKNUM_0  = ''
    AND S.XSPTSDH_0     <> 2;
GO
