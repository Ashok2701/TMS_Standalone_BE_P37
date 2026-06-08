package com.transport.tms.Fleet.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;


@Entity
@Data
@Table(name = "XX10CCLASS")
public class VehClass {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "ROWID")
//    private Long rowid;

    @Column(name = "CLASS_0")
    private String className;
    @Column(name = "DES_0")
    private String desc;
    @Column(name = "ENAFLG_0")
    private int enaFlag;
    @Column(name = "CRY_0")
    private String cry;
    @Column(name = "TYP_0")
    private int typ;
    @Column(name = "AXLNBR_0")
    private int axlnbr;
    @Column(name = "XMAXCAPW_0")
    private Double  xmaxcapw;
    @Column(name = "XMAXCAPV_0")
    private Double xmaxcapv;
    @Column(name = "CREDATTIM_0")
    private Date createDateTime;
    @Column(name = "UPDDATTIM_0")
    private Date updateDateTime;
//    @Column(name = "AUUID_0")
//    private byte[] auuid;
    @Id
    @Column(name = "AUUID_0")
    private UUID auuid;
    @Column(name = "CREUSR_0")
    private String creusr;
    @Column(name = "UPDUSR_0")
    private String updusr;
    @Column(name = "XMAXVUNIT_0")
    private String xmaxvunit;
    @Column(name = "XMAXUNIT_0")
    private String xmaxunit;
    @Column(name = "XSKILLNO_0")
    private int xskillno;
    @Column(name = "XINSPIN_0")
    private String xinspin;
    @Column(name = "XMANIN_0")
    private int xmanin;
    @Column(name = "XINSPOUT_0")
    private String xinspout;
    @Column(name = "XMANOUT_0")
    private int xmanout;

    @PrePersist
    @PreUpdate
    public void setDefaultValues() throws IllegalAccessException {
        Field[] columns = this.getClass().getDeclaredFields();
        for(Field column: columns){
            column.setAccessible(true);
            if(column.get(this)==null){
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
