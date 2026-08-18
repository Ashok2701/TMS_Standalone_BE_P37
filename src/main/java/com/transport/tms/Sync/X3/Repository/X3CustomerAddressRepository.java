package com.transport.tms.Sync.X3.Repository;

import com.transport.tms.Config.SchemaConfig;
import com.transport.tms.Sync.X3.Dto.X3CustomerAddressDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class X3CustomerAddressRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SchemaConfig schemas;

    public X3CustomerAddressRepository(
            @Qualifier("sqlServerJdbcTemplate") JdbcTemplate jdbcTemplate,
            SchemaConfig schemas) {
        this.jdbcTemplate = jdbcTemplate;
        this.schemas = schemas;
    }

    public Integer count() {
        String x3  = schemas.getX3Schema();
        // BUG FIX: was missing "WHERE A.BPATYP_0 = 1" — the exact filter
        // findCustomerAddresses() below actually uses. This made the "X3"
        // count shown in Sync History count every address type (all
        // BPATYP_0 values), not just the ones actually fetched/synced —
        // so it never matched Before/After/Inserted+Updated even when
        // the sync was working correctly, making it look like data was
        // missing when it wasn't.
        String sql = "SELECT COUNT(*) FROM " + x3 + ".BPADDRESS A"
                   + " INNER JOIN " + x3 + ".BPCUSTOMER C ON A.BPANUM_0 = C.BPCNUM_0"
                   + " WHERE A.BPATYP_0 = 1";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<X3CustomerAddressDTO> findCustomerAddresses() {
        String x3  = schemas.getX3Schema();
        String sql = "SELECT C.BPCNUM_0, A.BPAADD_0, A.BPADES_0,"
                   + " A.BPAADDLIG_0, A.BPAADDLIG_1, A.BPAADDLIG_2,"
                   + " A.CTY_0, A.POSCOD_0, A.SAT_0, A.CRY_0, A.CRYNAM_0,"
                   + " A.TEL_0, A.MOB_0, A.WEB_0,"
                   + " CASE WHEN C.BPAADD_0 = A.BPAADD_0 THEN 1 ELSE 0 END AS IS_DEFAULT"
                   + " FROM " + x3 + ".BPADDRESS A"
                   + " INNER JOIN " + x3 + ".BPCUSTOMER C ON A.BPANUM_0 = C.BPCNUM_0"
                   + " WHERE A.BPATYP_0 = 1 ORDER BY C.BPCNUM_0, IS_DEFAULT DESC";

        return jdbcTemplate.query(sql, (rs, row) -> {
            X3CustomerAddressDTO dto = new X3CustomerAddressDTO();
            dto.setCustomerCode(rs.getString("BPCNUM_0"));
            dto.setAddressCode(rs.getString("BPAADD_0"));
            dto.setAddressDescription(rs.getString("BPADES_0"));
            dto.setAddressLine1(rs.getString("BPAADDLIG_0"));
            dto.setAddressLine2(rs.getString("BPAADDLIG_1"));
            dto.setAddressLine3(rs.getString("BPAADDLIG_2"));
            dto.setCity(rs.getString("CTY_0"));
            dto.setPostalCode(rs.getString("POSCOD_0"));
            dto.setStateCode(rs.getString("SAT_0"));
            dto.setCountryCode(rs.getString("CRY_0"));
            dto.setCountryName(rs.getString("CRYNAM_0"));
            dto.setPhone(rs.getString("TEL_0"));
            dto.setMobile(rs.getString("MOB_0"));
            dto.setWebSite(rs.getString("WEB_0"));
            dto.setDefaultAddress(rs.getInt("IS_DEFAULT") == 1);
            return dto;
        });
    }
}
