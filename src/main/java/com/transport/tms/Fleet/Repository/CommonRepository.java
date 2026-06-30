package com.transport.tms.Fleet.Repository;

import com.transport.tms.Fleet.Entity.DropdownData;
import com.transport.tms.Fleet.Entity.PostalCodeDetails;
import com.transport.tms.Fleet.Entity.StyleData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Most queries here target X3 tables (FACILITY, BPCARRIER, XX10CCLASS, etc.)
 * which live in SQL Server (tbs / LEWISB schema) via sqlServerJdbcTemplate.
 *
 * EXCEPTION: driver lists use Postgres tms.xr_driver (TMS master data,
 * synced separately) instead of X3's XX10CDRIVER.
 */
@Repository
public class CommonRepository {

    private final JdbcTemplate sqlServerJdbc;
    private final DriverRepository driverRepository;

    @Value("${x3.schema}")
    private String dbSchema;   // X3 schema, e.g. "LEWISB" — separate from Postgres db.schema

    public CommonRepository(@Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbc,
                            DriverRepository driverRepository) {
        this.sqlServerJdbc = sqlServerJdbc;
        this.driverRepository = driverRepository;
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

    // Uses Postgres tms.xr_driver (TMS master data) instead of X3 XX10CDRIVER
    public List<DropdownData> getDriverList() {
        return driverRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getActive()))
                .map(d -> new DropdownData(
                        d.getDriverId()   != null ? d.getDriverId()   : "",
                        d.getDriverName() != null ? d.getDriverName() : ""))
                .sorted((a, b) -> String.valueOf(a.getValue()).compareTo(String.valueOf(b.getValue())))
                .toList();
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

    // Uses Postgres tms.xr_driver instead of X3 XX10CDRIVER
    public List<Map<String, Object>> getDriverData() {
        return driverRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getActive()))
                .map(d -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("driverId",    d.getDriverId());
                    m.put("driverName",  d.getDriverName());
                    m.put("mobile",      d.getMobileNo());
                    m.put("licenseType", d.getLicenseType());
                    m.put("licenseNum",  d.getLicenseNumber());
                    m.put("site",        null); // not on Driver entity
                    return m;
                })
                .toList();
    }
}
