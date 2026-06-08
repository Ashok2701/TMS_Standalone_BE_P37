package com.transport.tms.transport.sqlserver.Entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
    @Setter
    @Entity
    @Table(name = "\"XTMSUSRFCY\"", schema = "\"LEWISB\"")
    public class sites {

        @Column(name= "XFCY_0")
        private String fcy;
        @Column(name= "DEFFLG")
        private String defflg;
        @Column(name= "FCYNAM_0")
        private String fcynam;
        @Column(name= "CRY_0")
        private String cry;
        @Column(name= "XX10C_GEOX_0")
        private String xx10c_geox;
        @Column(name= "XX10C_GEOY_0")
        private String xx10c_geoy;
        @Column(name = "XTMSFCY_0")
        private int fcyNumber;
        @Id
        @Column(name= "ROWID")
        private int rowid;
}
