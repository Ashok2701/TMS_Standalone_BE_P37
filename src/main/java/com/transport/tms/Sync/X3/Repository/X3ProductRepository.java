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

        // ITMMASTER = base item master table in X3
        // Each item has one row per company (CPY_0)
        String sql = """
            SELECT COUNT(DISTINCT ITMREF_0)
            FROM LEWISB.ITMMASTER
        """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        System.out.println("X3 PRODUCT COUNT (ITMMASTER) = " + count);
        return count;
    }

    public List<X3ProductDTO> findProducts() {

        // ITMMASTER  = base item record (ITMREF_0, TCLCOD_0, UOM_0, weights etc)
        // ITMDES     = item descriptions per language (ITMDES1_0, ITMDES2_0)
        // Join on ITMREF_0, take English (LAN_0 = 'ENG') or first available language
        String sql = """
            SELECT
                M.ITMREF_0,
                ISNULL(D.ITMDES1_0, '')   AS ITMDES1_0,
                ISNULL(D.ITMDES2_0, '')   AS ITMDES2_0,
                ISNULL(M.TCLCOD_0, '')    AS TCLCOD_0,
                ISNULL(M.UOM_0, '')       AS UOM_0,
                ISNULL(M.SAU_0, '')       AS SAU_0,
                ISNULL(M.NETWEI_0, 0)     AS NETWEI_0,
                ISNULL(M.GROWEI_0, 0)     AS GROWEI_0,
                ISNULL(M.VOL_0, 0)        AS VOL_0,
                ISNULL(M.WEU_0, '')       AS WEU_0,
                ISNULL(M.VOU_0, '')       AS VOU_0,
                ISNULL(M.ENAFLG_0, 2)     AS ENAFLG_0
            FROM LEWISB.ITMMASTER M
            LEFT JOIN LEWISB.ITMDES D
                ON  D.ITMREF_0 = M.ITMREF_0
                AND D.LANNUM_0 = (
                    SELECT MIN(LANNUM_0)
                    FROM LEWISB.ITMDES
                    WHERE ITMREF_0 = M.ITMREF_0
                )
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
                    // ENAFLG_0 = 2 means active in X3
                    dto.setActive(rs.getInt("ENAFLG_0") == 2);

                    return dto;
                }
        );

        System.out.println("X3 PRODUCTS FETCHED = " + result.size());
        return result;
    }
}
