package com.transport.tms.Sync.X3.Repository;

import com.transport.tms.Config.SchemaConfig;


import com.transport.tms.Sync.X3.Dto.X3SiteDTO;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class X3SiteRepository {


    private final JdbcTemplate jdbcTemplate;
    private final SchemaConfig schemas;



    public X3SiteRepository(
            @Qualifier("sqlServerJdbcTemplate",
            SchemaConfig schemas)
            JdbcTemplate jdbcTemplate
    ,
            SchemaConfig schemas){

        this.jdbcTemplate = jdbcTemplate;

    
        this.schemas = schemas;
    }




    public Integer count(){


        String sql = """

            SELECT COUNT(*)
            FROM " + schemas.getX3Schema() + ".FACILITY

        """;


        Integer count =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class
                );


        System.out.println(
                "SQL SERVER SITE COUNT = "
                        + count
        );


        return count;

    }






    public List<X3SiteDTO> findSites(){



        String sql = """

        SELECT

        F.FCY_0,
        F.FCYNAM_0,
        F.FCYSHO_0,
        F.CRY_0,

        F.BPAADD_0,

        A.BPADES_0,
        A.BPAADDLIG_0,
        A.BPAADDLIG_1,
        A.BPAADDLIG_2,

        A.POSCOD_0,
        A.CTY_0,
        A.SAT_0,
        A.CRYNAM_0

        FROM " + schemas.getX3Schema() + ".FACILITY F

        LEFT JOIN " + schemas.getX3Schema() + ".BPADDRESS A

        ON F.BPAADD_0 = A.BPAADD_0

        """;




        return jdbcTemplate.query(

                sql,


                (rs,row)->{


                    X3SiteDTO dto =
                            new X3SiteDTO();



                    dto.setSiteCode(
                            rs.getString("FCY_0")
                    );


                    dto.setSiteName(
                            rs.getString("FCYNAM_0")
                    );


                    dto.setShortName(
                            rs.getString("FCYSHO_0")
                    );


                    dto.setCountryCode(
                            rs.getString("CRY_0")
                    );


                    dto.setAddressCode(
                            rs.getString("BPAADD_0")
                    );


                    dto.setAddressDescription(
                            rs.getString("BPADES_0")
                    );


                    dto.setAddressLine1(
                            rs.getString("BPAADDLIG_0")
                    );


                    dto.setAddressLine2(
                            rs.getString("BPAADDLIG_1")
                    );


                    dto.setAddressLine3(
                            rs.getString("BPAADDLIG_2")
                    );


                    dto.setPostalCode(
                            rs.getString("POSCOD_0")
                    );


                    dto.setCity(
                            rs.getString("CTY_0")
                    );


                    dto.setStateCode(
                            rs.getString("SAT_0")
                    );


                    dto.setCountryName(
                            rs.getString("CRYNAM_0")
                    );


                    return dto;


                }
        );

    }

}