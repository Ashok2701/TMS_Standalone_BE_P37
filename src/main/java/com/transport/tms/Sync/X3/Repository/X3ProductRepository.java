package com.transport.tms.Sync.X3.Repository;

import com.transport.tms.Sync.X3.Dto.X3ProductDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class X3ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public X3ProductRepository(
            @Qualifier("sqlServerJdbcTemplate")
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer count() {

        // ITMMASTER is the standard X3 item master table
        // If this returns 0, verify the correct table name in your X3 SQL Server schema
        // Common alternatives: ITMMASTER, ITMBPS, ITMFACILIT
        String sql = """
            SELECT COUNT(*)
            FROM LEWISB.ITMMASTER
        """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        System.out.println("X3 PRODUCT COUNT = " + count);
        return count;
    }

    public List<X3ProductDTO> findProducts() {

        String sql = """
            SELECT
                I.ITMREF_0,
                I.ITMDES1_0,
                I.ITMDES2_0,
                I.TCLCOD_0,
                I.UOM_0,
                I.SAU_0,
                I.NETWEI_0,
                I.GROWEI_0,
                I.VOL_0,
                I.WEU_0,
                I.VOU_0,
                I.ENAFLG_0
            FROM LEWISB.ITMMASTER I
        """;

        List<X3ProductDTO> result = jdbcTemplate.query(
                sql,
                (rs, row) -> {

                    X3ProductDTO dto = new X3ProductDTO();

                    dto.setProductCode(rs.getString("ITMREF_0"));
                    dto.setProductName(rs.getString("ITMDES1_0"));
                    dto.setShortDescription(rs.getString("ITMDES2_0"));
                    dto.setProductCategory(rs.getString("TCLCOD_0"));
                    dto.setUnitOfMeasure(rs.getString("UOM_0"));
                    dto.setSalesUnit(rs.getString("SAU_0"));
                    dto.setNetWeight(rs.getDouble("NETWEI_0"));
                    dto.setGrossWeight(rs.getDouble("GROWEI_0"));
                    dto.setVolume(rs.getDouble("VOL_0"));
                    dto.setWeightUnit(rs.getString("WEU_0"));
                    dto.setVolumeUnit(rs.getString("VOU_0"));
                    dto.setActive(rs.getInt("ENAFLG_0") == 2);

                    return dto;
                }
        );

        System.out.println("X3 PRODUCTS FETCHED = " + result.size());
        return result;
    }
}
