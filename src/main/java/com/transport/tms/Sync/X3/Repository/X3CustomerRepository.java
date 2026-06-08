package com.transport.tms.Sync.X3.Repository;

import com.transport.tms.Sync.X3.Dto.X3CustomerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class X3CustomerRepository {

    @Qualifier("sqlServerJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    public Integer count() {

        String sql = """
            SELECT COUNT(*)
            FROM LEWISB.BPCUSTOMER
        """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        System.out.println("X3 CUSTOMER COUNT = " + count);
        return count;
    }

    public List<X3CustomerDTO> findCustomers() {

        String sql = """
            SELECT
                                                          BPCNUM_0,
                                                          BPCNAM_0,
                                                          BPCSHO_0,
                                                          BCGCOD_0 AS  CRY_0,
                                                          CUR_0
                
                                                      FROM LEWISB.BPCUSTOMER
        """;

        List<X3CustomerDTO> result = jdbcTemplate.query(
                sql,
                (rs, row) -> {

                    X3CustomerDTO dto = new X3CustomerDTO();

                    dto.setCustomerCode(rs.getString("BPCNUM_0"));
                    dto.setCustomerName(rs.getString("BPCNAM_0"));
                    dto.setShortName(rs.getString("BPCSHO_0"));
                    dto.setCountryCode(rs.getString("CRY_0"));
                    dto.setCurrencyCode(rs.getString("CUR_0"));
                    dto.setActive(true);

                    return dto;
                }
        );

        System.out.println("X3 CUSTOMERS FETCHED = " + result.size());
        return result;
    }
}
