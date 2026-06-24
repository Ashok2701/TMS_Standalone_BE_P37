package com.transport.tms.RoutePlanner.Repository;

import com.transport.tms.RoutePlanner.Dto.RoutePlannerStopDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class X3RoutePlannerRepository {

    @Qualifier("sqlServerJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    // ─────────────────────────────────────────────────────────
    // DROPS  —  LEWISB.XTMSDLVY_TMS
    // ─────────────────────────────────────────────────────────
    public List<RoutePlannerStopDTO> findDropsByDateAndSite(
            String siteCode, LocalDate planDate) {

        String sql = """
            SELECT *
            FROM   LEWISB.XTMSDLVY_TMS
            WHERE  SITE    = ?
              AND  DOCDATE = ?
            ORDER BY SEQ, DOCNUM
        """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> mapStop(rs),
                siteCode, Date.valueOf(planDate));
    }

    // ─────────────────────────────────────────────────────────
    // PICKUPS  —  TMSNEW.XTMSPICK_TMS
    // ─────────────────────────────────────────────────────────
    public List<RoutePlannerStopDTO> findPickupsByDateAndSite(
            String siteCode, LocalDate planDate) {

        String sql = """
            SELECT *
            FROM   LEWISB.XTMSPICK_TMS
            WHERE  SITE    = ?
              AND  DOCDATE = ?
            ORDER BY SEQ, DOCNUM
        """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> mapStop(rs),
                siteCode, Date.valueOf(planDate));
    }

    // ─────────────────────────────────────────────────────────
    // COMMON ROW MAPPER
    // Both views expose identical column aliases so one mapper
    // handles DLV and PICK. DOCTYPE distinguishes them.
    // ─────────────────────────────────────────────────────────
    private RoutePlannerStopDTO mapStop(ResultSet rs) throws SQLException {

        RoutePlannerStopDTO dto = new RoutePlannerStopDTO();

        // Identity
        dto.setSite(rs.getString("SITE"));
        dto.setDocNum(rs.getString("DOCNUM"));
        dto.setDocType(rs.getString("DOCTYPE"));       // 'DLV' or 'PICK'
        dto.setMovType(rs.getString("MOVTYPE"));
        dto.setStopType(
            "DLV".equals(rs.getString("DOCTYPE")) ? "DROP" : "PICKUP"
        );

        // Dates
        dto.setDocDate(toLocal(rs.getDate("DOCDATE")));
        dto.setOriginalDeliveryDate(toLocal(rs.getDate("OGLDLVDATE")));
        dto.setCompanyCode(rs.getString("CPYCODE"));

        // Status
        dto.setDeliveryStatus(rs.getObject("DLVYSTATUS", Integer.class));
        dto.setRouteStatus(rs.getString("ROUTESTATUS"));

        // Priority
        dto.setPriority(rs.getObject("PRIORITY", Integer.class));

        // Route
        dto.setRouteCode(rs.getString("ROUTECODE"));
        dto.setRouteCodeDesc(rs.getString("ROUTECODEDESC"));
        dto.setRouteCodeBgColor(rs.getString("ROUTECODEBGCLR"));

        // Business partner
        dto.setBpCode(rs.getString("BPCODE"));
        dto.setBpName(rs.getString("BPNAME"));
        dto.setAddressCode(rs.getString("ADRESCODE"));
        dto.setAddressName(rs.getString("ADRESNAME"));

        // Address
        dto.setAddLine1(rs.getString("ADDLIG1"));
        dto.setAddLine2(rs.getString("ADDLIG2"));
        dto.setAddLine3(rs.getString("ADDLIG3"));
        dto.setPosCode(rs.getString("POSCODE"));
        dto.setCity(rs.getString("CITY"));
        dto.setStateCode(rs.getString("STATECODE"));
        dto.setCountryCode(rs.getString("COUNTRYCODE"));
        dto.setCountryName(rs.getString("COUNTRYNAME"));

        // Weight / Volume
        dto.setNbPack(rs.getBigDecimal("NBPACK"));
        dto.setNetWeight(rs.getBigDecimal("NETWEIGHT"));
        dto.setWeightUnit(rs.getString("WEIGHTUNIT"));
        dto.setVolume(rs.getBigDecimal("VOLUME"));
        dto.setVolumeUnit(rs.getString("VOLUME_UNIT"));

        // Driver / Vehicle
        dto.setDriverCode(rs.getString("DRIVERCODE"));
        dto.setVehicleCode(rs.getString("VEHICLECODE"));
        dto.setVehiclePlate(rs.getString("VEHICLEPLATE"));

        // Trip
        dto.setTripNo(rs.getString("TRIPNO"));
        dto.setDlvMode(rs.getString("DLVMODE"));
        dto.setVrCode(rs.getString("VRCODE"));
        dto.setVrSeq(rs.getString("VRSEQ"));
        dto.setSeq(rs.getObject("SEQ", Integer.class));

        // Dep / Arv
        dto.setDepDate(toLocal(rs.getDate("DEPDATE")));
        dto.setDepTime(rs.getString("DEPTIME"));
        dto.setArvDate(toLocal(rs.getDate("ARVDATE")));
        dto.setArvTime(rs.getString("ARVTIME"));

        // Carrier
        dto.setCarrier(rs.getString("CARRIER"));
        dto.setCarrColor(rs.getString("CARRCOLOR"));
        dto.setDocInst(rs.getString("DOCINST"));

        return dto;
    }

    private LocalDate toLocal(Date d) {
        return d != null ? d.toLocalDate() : null;
    }
}
