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
            @Qualifier("sqlServerJdbcTemplate")
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    // COUNT must match exactly what findCustomerAddresses() fetches
    // — all addresses in BPADDRESS that belong to a customer
    public Integer count() {

        String sql = """
            SELECT COUNT(*)
            FROM " + schemas.getX3Schema() + ".BPADDRESS A
            INNER JOIN " + schemas.getX3Schema() + ".BPCUSTOMER C
                ON A.BPANUM_0 = C.BPCNUM_0
        """;

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    // Fetch ALL addresses for ALL customers
    // BPADDRESS.BPANUM_0  = the customer code this address belongs to
    // BPADDRESS.BPAADD_0  = the address code (unique key)
    public List<X3CustomerAddressDTO> findCustomerAddresses() {

        String sql = """
            SELECT
                C.BPCNUM_0,
                A.BPAADD_0,
                A.BPADES_0,
                A.BPAADDLIG_0,
                A.BPAADDLIG_1,
                A.BPAADDLIG_2,
                A.CTY_0,
                A.POSCOD_0,
                A.SAT_0,
                A.CRY_0,
                A.CRYNAM_0,
                A.TEL_0,
                A.MOB_0,
                A.WEB_0,
                CASE WHEN C.BPAADD_0 = A.BPAADD_0 THEN 1 ELSE 0 END AS IS_DEFAULT
            FROM " + schemas.getX3Schema() + ".BPADDRESS A
            INNER JOIN " + schemas.getX3Schema() + ".BPCUSTOMER C
                ON A.BPANUM_0 = C.BPCNUM_0
            ORDER BY C.BPCNUM_0, IS_DEFAULT DESC
        """;

        return jdbcTemplate.query(
                sql,
                (rs, row) -> {

                    X3CustomerAddressDTO dto =
                            new X3CustomerAddressDTO();

                    dto.setCustomerCode(
                            rs.getString("BPCNUM_0"));

                    dto.setAddressCode(
                            rs.getString("BPAADD_0"));

                    dto.setAddressDescription(
                            rs.getString("BPADES_0"));

                    dto.setAddressLine1(
                            rs.getString("BPAADDLIG_0"));

                    dto.setAddressLine2(
                            rs.getString("BPAADDLIG_1"));

                    dto.setAddressLine3(
                            rs.getString("BPAADDLIG_2"));

                    dto.setCity(
                            rs.getString("CTY_0"));

                    dto.setPostalCode(
                            rs.getString("POSCOD_0"));

                    dto.setStateCode(
                            rs.getString("SAT_0"));

                    dto.setCountryCode(
                            rs.getString("CRY_0"));

                    dto.setCountryName(
                            rs.getString("CRYNAM_0"));

                    dto.setPhone(
                            rs.getString("TEL_0"));

                    dto.setMobile(
                            rs.getString("MOB_0"));

                    dto.setWebSite(
                            rs.getString("WEB_0"));

                    // true = this is the customer's primary/default address
                    dto.setDefaultAddress(
                            rs.getInt("IS_DEFAULT") == 1);

                    return dto;
                }
        );
    }
}
