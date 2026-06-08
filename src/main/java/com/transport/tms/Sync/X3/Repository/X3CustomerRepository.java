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



    public Integer count(){


        String sql = """

        SELECT COUNT(*)
        FROM LEWISB.BPCUSTOMER

        """;


        return jdbcTemplate.queryForObject(
                sql,
                Integer.class
        );
    }




    public List<X3CustomerDTO> findCustomers(){


        String sql = """

        SELECT

        BPCNUM_0,
        BPCNAM_0,
        BPCSHO_0,
        CRY_0,
        CUR_0,
        ENAFLG_0

        FROM LEWISB.BPCUSTOMER

        """;


        return jdbcTemplate.query(
                sql,

                (rs,row)->{


                    X3CustomerDTO dto =
                            new X3CustomerDTO();


                    dto.setCustomerCode(
                            rs.getString("BPCNUM_0"));


                    dto.setCustomerName(
                            rs.getString("BPCNAM_0"));


                    dto.setShortName(
                            rs.getString("BPCSHO_0"));


                    dto.setCountryCode(
                            rs.getString("CRY_0"));


                    dto.setCurrencyCode(
                            rs.getString("CUR_0"));


                    dto.setActive(
                            rs.getInt("ENAFLG_0")==2);


                    return dto;

                }
        );
    }

}