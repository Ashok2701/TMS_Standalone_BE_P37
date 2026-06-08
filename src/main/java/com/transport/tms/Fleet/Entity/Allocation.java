package com.transport.tms.Fleet.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Date;


@Entity
@Data
@Table(name = "XX10CALLOC")
public class Allocation {

    @Column(name = "UPDTICK_0")
    private Integer updTick;

    @Column(name = "XTRANNO_0")
    private String transactionNumber;

    @Column(name = "XVEHICLE_0")
    private String vehicleNumber;

    @Column(name = "XDRIVER_0")
    private String driverId;

    @Column(name = "XDLICNUM_0")
    private String licenseNumber;

    @Column(name = "XSTRTDAT_0")
    private Date startDate;

    @Column(name = "XSTRTTIM_0")
    private String startTime;

    @Column(name = "XENDDAT_0")
    private Date endDate;

    @Column(name = "XENDTIM_0")
    private String endTime;

    @Column(name = "XLICENSETYPE_0")
    private Integer xlicensetype;

    @Column(name = "XTRUCKTYPE_0")
    private Integer truckType;

    @Column(name = "XUNITS_0")
    private String xUnits;

    @Column(name = "XUNITS1_0")
    private String xUnits1;

    @Column(name = "MOB_0")
    private String mobile;

    @Column(name = "CREDATTIM_0")
    private Date credAttim;

    @Column(name = "UPDDATTIM_0")
    private Date updDattim;

    @Column(name = "AUUID_0")
    private byte[] aUuid;

    @Column(name = "CREUSR_0")
    private String creUsr;

    @Column(name = "UPDUSR_0")
    private String updUsr;

    @Column(name = "XVEHCLASS_0")
    private String vehicleClass;

    @Column(name = "XALLMTRS_0")
    private Double odoStart;

    @Column(name = "XALLMTRE_0")
    private Double odoEnd;

    @Column(name = "XALLSTIME_0")
    private String xAllSTime;

    @Column(name = "XALLETIME_0")
    private String xAllETime;

    @Column(name = "XDNAME_0")
    private String driverName;

    @Column(name = "XDELINSPEC_0")
    private String xDelInspec;

    @Column(name = "XRETINSPEC_0")
    private String xRetInspec;

    @Column(name = "XMAN_0")
    private Integer xMan;

    @Column(name = "XDINSNUM_0")
    private String xDinsNum;

    @Column(name = "XRINSNUM_0")
    private String xRinsNum;

    @Column(name = "XVALIDATE_0")
    private Integer xValidate;

    @Column(name = "XVESTAT_0")
    private Integer status;

    @Column(name = "XVTIME_0")
    private String xVTime;

    @Column(name = "XVDATE_0")
    private Date xVDate;

    @Column(name = "XLICTYPE_0")
    private Integer licenseType;

    @Id
    @Column(name = "ROWID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rowId;


    @PrePersist
    @PreUpdate
    public void setDefaultValues() throws IllegalAccessException {
        Field[] columns = this.getClass().getDeclaredFields();
        for(Field column: columns){
            column.setAccessible(true);
            if(column.get(this)==null && !column.getName().equalsIgnoreCase("ROWID")){
                if(column.getType().equals(String.class)){
                    column.set(this, "");
                }else if(column.getType().equals(Integer.class)){
                    column.set(this, 0);
                }else if(column.getType().equals(Double.class)){
                    column.set(this, 0.0);
                }else if(column.getType().equals(Date.class)){
                    column.set(this, new Date());
                }else if(column.getType().equals(byte[].class)){
                    column.set(this, new byte[]{0});
                }
            }
        }
    }
}
