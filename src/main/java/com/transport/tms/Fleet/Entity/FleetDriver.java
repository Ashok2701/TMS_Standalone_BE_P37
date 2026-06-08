package com.transport.tms.Fleet.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "drivers")
public class FleetDriver {
    @Id
    @Column(name = "driverid_0")
    private String driverId;
    @Column(name = "driver_0")
    private String driver;
    @Column(name = "active_0")
    private Boolean active;
    @Column(name = "bptnum_0")
    private String bptnum;
    @Column(name = "lanmain_0")
    private String lanmain;
    @Column(name = "lansec_0")
    private String lansec;
    @Column(name = "cry_0")
    private String cry;
    @Column(name = "bir_0")
    private Date bir;
    @Column(name = "credattim_0")
    private Date credattim;
    @Column(name = "upddattim_0")
    private Date upddattim;
    @Column(name = "auuid_0")
    private UUID auuid;
    @Column(name = "creusr_0")
    private String creusr;
    @Column(name = "updusr_0")
    private String updusr;
    @Column(name = "bpaaddlig_0")
    private String bpaaddlig0;
    @Column(name = "bpaaddlig_1")
    private String bpaaddlig1;
    @Column(name = "bpaaddlig_2")
    private String bpaaddlig2;
    @Column(name = "poscod_0")
    private String poscod;
    @Column(name = "cty_0")
    private String cty;
    @Column(name = "mob_0")
    private String mob;
    @Column(name = "web_0")
    private String web;
    @Column(name = "tel_0")
    private String tel0;
    @Column(name = "tel_1")
    private String tel1;
    @Column(name = "tel_2")
    private String tel2;
    @Column(name = "tel_3")
    private String tel3;
    @Column(name = "tel_4")
    private String tel4;
    @Column(name = "licenum_0")
    private String licenum;
    @Column(name = "licedat_0")
    private Date licedat;
    @Column(name = "licetyp_0")
    private Short licetyp;
    @Column(name = "validat_0")
    private Date validat;
    @Column(name = "delivby_0")
    private String delivby;
    @Column(name = "lastvime_0")
    private Date lastvime;
    @Column(name = "xuser_0")
    private String xuser;
    @Column(name = "xpwd_0")
    private String xpwd;
    @Column(name = "xpwd1_0")
    private String xpwd1;
    @Column(name = "xsigcon_0")
    private Boolean xsigcon;
    @Column(name = "xcamcon_0")
    private Boolean xcamcon;
    @Column(name = "xper_0")
    private Boolean xper;
    @Column(name = "xlgflg_0")
    private Integer xlgnflg;
    @Column(name = "xbus_0")
    private String xbus;
    @Column(name = "xloginseqno_0")
    private String xloginseqno;
    @Column(name = "xx10c_geox_0")
    private String xx10cGeox;
    @Column(name = "xx10c_geoy_0")
    private String xx10cGeoy;
    @Column(name = "xlncstarttim_0")
    private String xlncstarttim;
    @Column(name = "xlncdur_0")
    private String xlncdur;
    @Column(name = "styzon_0")
    private String styzon;
    @Column(name = "xpaycon_0")
    private Boolean xpaycon;
    @Column(name = "xuvycod_0")
    private String xuvycod;
    @Column(name = "xskpcon_0")
    private Boolean xskpcon;
    @Column(name = "xrescon_0")
    private Boolean xrescon;
    @Column(name = "xqtychgcon_0")
    private Boolean xqtychgcon;
    @Column(name = "xspotcon_0")
    private Boolean xspotcon;
    @Column(name = "xpickupcon_0")
    private Boolean xpickupcon;
    @Column(name = "xsihcon_0")
    private Boolean xsihcon;
    @Column(name = "xlogflag_0")
    private Short xlogflag;
    @Column(name = "xmacadd_0")
    private String xmacadd;
    @Column(name = "xdeposit_0")
    private Boolean xdeposit;
    @Column(name = "xgeocon_0")
    private Boolean xgeocon;
    @Column(name = "xnooftrips_0")
    private Integer xnooftrips;
    @Column(name = "xsalerep_0")
    private Boolean xsalerep;
    @Column(name = "xdriver_0")
    private Boolean xdriver;
    @Column(name = "xdsd_0")
    private Boolean xdsd;
    @Column(name = "xsalrout_0")
    private Boolean xsalrout;
    @Column(name = "x10cmon_0")
    private Boolean x10cmon;
    @Column(name = "x10ctues_0")
    private Boolean x10ctues;
    @Column(name = "x10cwed_0")
    private Boolean x10cwed;
    @Column(name = "x10cthu_0")
    private Boolean x10cthu;
    @Column(name = "x10cfri_0")
    private Boolean x10cfri;
    @Column(name = "x10csat_0")
    private Boolean x10csat;
    @Column(name = "x10csun_0")
    private Boolean x10csun;
    @Column(name = "x1coverhrs_0")
    private Integer x1coverhrs;
    @Column(name = "xmaxhrsday_0")
    private Integer xmaxhrsday;
    @Column(name = "xmaxhrsweek_0")
    private Integer xmaxhrsweek;
    @Column(name = "xdriverhrs_0")
    private Integer xdriverhrs;
    @Column(name = "xallvehicle_0")
    private Boolean xallvehicle;
    @Column(name = "xuvystrdat_0")
    private Date xuvystrdat;
    @Column(name = "xuvyenddat_0")
    private Date xuvyenddat;
    @Column(name = "xlonghaul_0")
    private Boolean xlonghaul;
    @Column(name = "note_0")
    private String note;
    @Column(name = "fcy_0")
    private String fcy;
    @Column(name = "sat_0")
    private String sat;
    @Column(name = "driverimage")
    private byte[] driverimage;

    @PrePersist
    @PreUpdate
    public void setDefaultValues() throws IllegalAccessException {
        Field[] columns = this.getClass().getDeclaredFields();
        for(Field column: columns){
            column.setAccessible(true);
            if(column.get(this)==null && !column.getName().equalsIgnoreCase("ROWID")){
                if(column.getType().equals(String.class)){
                    column.set(this, "");
                }else if(column.getType().equals(Integer.class) || column.getType().equals(int.class)){
                    column.set(this, 0);
                }else if(column.getType().equals(Short.class) || column.getType().equals(short.class)){
                    column.set(this, (short) 0);
                }else if(column.getType().equals(Double.class) || column.getType().equals(double.class)){
                    column.set(this, 0.0);
                }else if(column.getType().equals(Date.class)){
                    column.set(this, new Date());
                }else if(column.getType().equals(Boolean.class) || column.getType().equals(boolean.class)){
                    column.set(this, false);
                }else if(column.getType().equals(UUID.class)){
                    column.set(this, UUID.randomUUID());
                }
            }
        }
    }
}
