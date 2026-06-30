package com.transport.tms.Fleet.Repository;

import com.transport.tms.Fleet.Entity.DropdownData;
import com.transport.tms.Fleet.Entity.PostalCodeDetails;
import com.transport.tms.Fleet.Entity.StyleData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All queries here target X3 tables (FACILITY, BPCARRIER, XX10CDRIVER, etc.)
 * which live in SQL Server (tbs / LEWISB schema) — NOT Postgres.
 * Uses sqlServerJdbcTemplate, never the default JPA EntityManager.
 */
@Repository
public class CommonRepository {

    private final JdbcTemplate sqlServerJdbc;

    @Value("${x3.schema}")
    private String dbSchema;   // X3 schema, e.g. "LEWISB" — separate from Postgres db.schema

    public CommonRepository(@Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbc) {
        this.sqlServerJdbc = sqlServerJdbc;
    }

    private List<DropdownData> queryDropdown(String sql) {
        return sqlServerJdbc.query(sql, (rs, n) -> new DropdownData(
                rs.getString(1) != null ? rs.getString(1) : "",
                rs.getString(2) != null ? rs.getString(2) : ""
        ));
    }

    public List<DropdownData> getSiteList() {
        return queryDropdown("select FCY_0 as value, FCYNAM_0 as label from " + dbSchema + ".FACILITY");
    }

    public List<DropdownData> getCarrierList() {
        return queryDropdown("select BPTNUM_0, BPTNAM_0 from " + dbSchema + ".BPCARRIER");
    }

    public List<DropdownData> getBusinessLineList() {
        return queryDropdown(
            "select distinct IDENT2_0, TEXTE_0 from " + dbSchema + ".ATEXTRA " +
            "where IDENT1_0='425' and IDENT2_0 in (select CODE_0 from " + dbSchema + ".ATABDIV where NUMTAB_0=425) " +
            "and LANGUE_0 = 'ENG' and ZONE_0='LNGDES' order by IDENT2_0");
    }

    public List<DropdownData> getPrimaryLanguageList() {
        return queryDropdown(
            "select IDENT1_0, TEXTE_0 from " + dbSchema + ".ATEXTRA where IDENT1_0 in (select LAN_0 from " + dbSchema + ".TABLAN) " +
            "and ZONE_0='INTDES' and LANGUE_0='ENG' order by IDENT1_0");
    }

    public List<StyleData> getStyleList() {
        String sql = "select distinct DES_0, COD_0, STY_0 from " + dbSchema + ".ASTYLE order by COD_0";
        return sqlServerJdbc.query(sql, (rs, n) -> new StyleData(
                rs.getString("COD_0") != null ? rs.getString("COD_0") : "",
                rs.getString("DES_0") != null ? rs.getString("DES_0") : "",
                rs.getString("STY_0") != null ? rs.getString("STY_0") : ""
        ));
    }

    public List<DropdownData> getUnAvailableList() {
        return queryDropdown(
            "select IDENT1_0, TEXTE_0 from " + dbSchema + ".ATEXTRA " +
            "where IDENT1_0 in (select UVYCOD_0 from " + dbSchema + ".TABUNAVAIL) " +
            "and LANGUE_0='ENG' and TEXTE_0<>'' and CODFIC_0='TABUNAVAIL' " +
            "and ZONE_0='DESAXX' order by IDENT1_0");
    }

    public List<DropdownData> getCountryList() {
        return queryDropdown(
            "select distinct IDENT1_0, TEXTE_0 from " + dbSchema + ".ATEXTRA where IDENT1_0 in (select CRY_0 from " + dbSchema + ".TABCOUNTRY) " +
            "and CODFIC_0='TABCOUNTRY' and TEXTE_0<>'' and LANGUE_0='ENG' and ZONE_0='CRYDES' " +
            "order by IDENT1_0");
    }

    public List<PostalCodeDetails> getPostalDetailsList(String country) {
        String sql = "select CRY_0, POSCOD_0, POSCTY_0, SATCOD_0 from " + dbSchema + ".POSCOD where CRY_0=?";
        return sqlServerJdbc.query(sql, (rs, n) -> new PostalCodeDetails(
                rs.getString("CRY_0")    != null ? rs.getString("CRY_0")    : "",
                rs.getString("POSCOD_0") != null ? rs.getString("POSCOD_0") : "",
                rs.getString("POSCTY_0") != null ? rs.getString("POSCTY_0") : "",
                rs.getString("SATCOD_0") != null ? rs.getString("SATCOD_0") : ""
        ), country);
    }

    public List<DropdownData> getInspectionList() {
        return queryDropdown("select XID_0, XQDES_0 from " + dbSchema + ".XINSQUEH order by XID_0");
    }

    public List<DropdownData> getFixedAssetList() {
        return queryDropdown("select AASREF_0, AASDES1_0 from " + dbSchema + ".FXDASSETS order by AASREF_0");
    }

    public List<DropdownData> getVehicleFuelUnitList() {
        return queryDropdown(
            "select IDENT1_0, TEXTE_0 FROM " + dbSchema + ".ATEXTRA " +
            "where CODFIC_0 = 'TABUNIT' and LANGUE_0='ENG' and ZONE_0='DES'");
    }

    public List<DropdownData> getDriverList() {
        return queryDropdown(
            "select DRIVERID_0, DRIVER_0 from " + dbSchema + ".XX10CDRIVER order by DRIVERID_0");
    }

    public List<DropdownData> getCustomerList() {
        return queryDropdown("select BPCNUM_0, BPCNAM_0 from " + dbSchema + ".BPCUSTOMER order by BPCNUM_0");
    }

    public List<DropdownData> getCategoryList() {
        return queryDropdown(
            "select distinct TCLCOD_0, TEXTE_0 from " + dbSchema + ".ITMCATEG i " +
            "join " + dbSchema + ".ATEXTRA a on a.IDENT1_0 = i.TCLCOD_0 " +
            "where a.CODFIC_0='ITMCATEG' and a.LANGUE_0='ENG' and a.ZONE_0='TCLAXX' " +
            "order by TCLCOD_0");
    }

    public List<DropdownData> getVehicleClassList() {
        return queryDropdown("select CLASS_0, DES_0 from " + dbSchema + ".XX10CCLASS order by CLASS_0");
    }

    public List<DropdownData> getTrailerTypeList() {
        return queryDropdown("select XTRACOD_0, XDES_0 from " + dbSchema + ".XX10CXTRA order by XTRACOD_0");
    }

    public List<DropdownData> getDocumentTypeList() {
        return queryDropdown(
            "select IDENT2_0, TEXTE_0 from " + dbSchema + ".ATEXTRA where ZONE_0='LNGDES' " +
            "and IDENT1_0=1502 and IDENT2_0 in (select CODE_0 from " + dbSchema + ".ATABDIV where NUMTAB_0=1502)");
    }

    public List<DropdownData> getissueAuthList() {
        return queryDropdown(
            "select IDENT2_0, TEXTE_0 from " + dbSchema + ".ATEXTRA where ZONE_0='LNGDES' and IDENT1_0=1503 " +
            "and IDENT2_0 in (select CODE_0 from " + dbSchema + ".ATABDIV where NUMTAB_0=1503)");
    }

    public List<DropdownData> getLocalMenuList(Integer type) {
        String sql = "select LANNUM_0, LANMES_0 from " + dbSchema + ".APLSTD " +
                     "where LANCHP_0=" + type + " and LAN_0='ENG' and LANNUM_0<>0";
        return queryDropdown(sql);
    }

    public List<Map<String, Object>> getVehicleData() {
        String sql = "select CODEYVE_0, CATEGO_0, XCODOMETER_0, FCY_0 from " + dbSchema + ".XX10CVEHICUL";
        return sqlServerJdbc.query(sql, (rs, n) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("vehicle",      rs.getObject("CODEYVE_0"));
            m.put("vehicleClass", rs.getObject("CATEGO_0"));
            m.put("odoStart",     rs.getObject("XCODOMETER_0"));
            m.put("site",         rs.getObject("FCY_0"));
            return m;
        });
    }

    public List<Map<String, Object>> getDriverData() {
        String sql = "select DRIVERID_0, DRIVER_0, MOB_0, LICETYP_0, LICENUM_0, FCY_0 from " + dbSchema + ".XX10CDRIVER";
        return sqlServerJdbc.query(sql, (rs, n) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("driverId",    rs.getObject("DRIVERID_0"));
            m.put("driverName",  rs.getObject("DRIVER_0"));
            m.put("mobile",      rs.getObject("MOB_0"));
            m.put("licenseType", rs.getObject("LICETYP_0"));
            m.put("licenseNum",  rs.getObject("LICENUM_0"));
            m.put("site",        rs.getObject("FCY_0"));
            return m;
        });
    }
}
