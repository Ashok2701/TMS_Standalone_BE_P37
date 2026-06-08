package com.transport.tms.Fleet.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Date;


@Data
@Entity
@Table(name = "XX10CTRAIL")
public class Trailer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROWID")
    private Long rowid;

    @Column(name = "UPDTICK_0")
    private Integer updTick;
    @Column(name = "TRAILER_0")
    private String trailer;
    @Column(name = "DES_0")
    private String des;
    @Column(name = "TYP_0")
    private String type;
    @Column(name = "LINKTO_0")
    private String linkTo;
    @Column(name = "MAXLEN_0")
    private Double maxLen;
    @Column(name = "MAXWID_0")
    private Double maxWid;
    @Column(name = "MAXFH_0")
    private Double maxFH;
    @Column(name = "MAXLOVOL_0")
    private Double maxLovol;
    @Column(name = "MAXLOAMS_0")
    private Double maxLoams;
    @Column(name = "CURBWEI_0")
    private Double curbWei;
    @Column(name = "GVWR_0")
    private Double gvwr;
    @Column(name = "NBAXLE_0")
    private Double nbaxle;
    @Column(name = "MAKE_0")
    private String make="";
    @Column(name = "MODEL_0")
    private String model="";
    @Column(name = "ANNEE_0")
    private int annee;
    @Column(name = "INSCHECK_0")
    private Date insCheck;
    @Column(name = "LASTINSP_0")
    private Date lastInsp;
    @Column(name = "COMMENT_0")
    private String comment;
    @Column(name = "CREDATTIM_0")
    private Date creatDateTime;
    @Column(name = "UPDDATTIM_0")
    private Date updateDateTime;
    @Column(name = "AUUID_0")
    private byte[] auuid;
    @Column(name = "CREUSR_0")
    private String creUser;
    @Column(name = "UPDUSR_0")
    private String updUser;
    @Column(name = "FCY_0")
    private String fcy;
    @Column(name = "XMAXLOVOL_0")
    private String xmaxlovol;
    @Column(name = "XMAXLOAMS_0")
    private String xmaxloams;
    @Column(name = "XTRKTYP_0")
    private Integer xtrktyp;
    @Column(name = "XTRACPY_0")
    private String xtracpy;
    @Column(name = "XADRCER_0")
    private String xadrcer;
    @Column(name = "XTRKISOA_0")
    private Integer xtrkisoa;
    @Column(name = "XTECDAT_0")
    private Date xtecdat;
    @Column(name = "XISOEXPIRY_0")
    private Date xisoexpiry;
    @Column(name = "XADRDAT_0")
    private Date xadrdat;
    @Column(name = "XWPRCHKD_0")
    private Date xwprchkd;
    @Column(name = "XHYDPRCHKD_0")
    private Date xhydprchkd;
    @Column(name = "XLOGTYPE1_0")
    private Integer xlogtype1;
    @Column(name = "XLOGTYPE2_0")
    private Integer xlogtype2;
    @Column(name = "XLOGTYPE3_0")
    private Integer xlogtype3;
    @Column(name = "XADRREQ_0")
    private Integer xadrreq;
    @Column(name = "XCISTYPE1_0")
    private Integer xcistype1;
    @Column(name = "XCISTYPE2_0")
    private Integer xcistype2;
    @Column(name = "XCISTYPE3_0")
    private Integer xcistype3;
    @Column(name = "XCISTYPE4_0")
    private Integer xcistype4;
    @Column(name = "XTADRAUCLS_0")
    private Integer xtadraucls;
    @Column(name = "XTADRAUCLS_1")
    private Integer xtadraucls1;
    @Column(name = "XTADRAUCLS_2")
    private Integer xtadraucls2;
    @Column(name = "XTADRAUCLS_3")
    private Integer xtadraucls3;
    @Column(name = "XTADRAUCLS_4")
    private Integer xtadraucls4;
    @Column(name = "XTADRAUCLS_5")
    private Integer xtadraucls5;
    @Column(name = "XTADRAUCLS_6")
    private Integer xtadraucls6;
    @Column(name = "XTADRAUCLS_7")
    private Integer xtadraucls7;
    @Column(name = "XTADRAUCLS_8")
    private Integer xtadraucls8;
    @Column(name = "XTADRAUCLS_9")
    private Integer xtadraucls9;
    @Column(name = "XTADRAUCL1_0")
    private Integer xtadraucl10;
    @Column(name = "XTADRAUCL1_1")
    private Integer xtadraucl11;
    @Column(name = "XTADRAUCL1_2")
    private Integer xtadraucl12;
    @Column(name = "XTADRAUCL1_3")
    private Integer xtadraucl13;
    @Column(name = "XTADRAUCL1_4")
    private Integer xtadraucl14;
    @Column(name = "XTADRAUCL1_5")
    private Integer xtadraucl15;
    @Column(name = "XTADRAUCL1_6")
    private Integer xtadraucl16;
    @Column(name = "XTADRAUCL1_7")
    private Integer xtadraucl17;
    @Column(name = "XTADRAUCL1_8")
    private Integer xtadraucl18;
    @Column(name = "XTADRAUCL1_9")
    private Integer xtadraucl19;
    @Column(name = "XTADRAUCL2_0")
    private Integer xtadraucl20;
    @Column(name = "XTADRAUCL2_1")
    private Integer xtadraucl21;
    @Column(name = "XTADRAUCL2_2")
    private Integer xtadraucl22;
    @Column(name = "XTADRAUCL2_3")
    private Integer xtadraucl23;
    @Column(name = "XTADRAUCL2_4")
    private Integer xtadraucl24;
    @Column(name = "XTADRAUCL2_5")
    private Integer xtadraucl25;
    @Column(name = "XTADRAUCL2_6")
    private Integer xtadraucl26;
    @Column(name = "XTADRAUCL2_7")
    private Integer xtadraucl27;
    @Column(name = "XTADRAUCL2_8")
    private Integer xtadraucl28;
    @Column(name = "XTADRAUCL2_9")
    private Integer xtadraucl29;
    @Column(name = "XTADRAUCL3_0")
    private Integer xtadraucl30;
    @Column(name = "XTADRAUCL3_1")
    private Integer xtadraucl31;
    @Column(name = "XTADRAUCL3_2")
    private Integer xtadraucl32;
    @Column(name = "XTADRAUCL3_3")
    private Integer xtadraucl33;
    @Column(name = "XTADRAUCL3_4")
    private Integer xtadraucl34;
    @Column(name = "XTADRAUCL3_5")
    private Integer xtadraucl35;
    @Column(name = "XTADRAUCL3_6")
    private Integer xtadraucl36;
    @Column(name = "XTADRAUCL3_7")
    private Integer xtadraucl37;
    @Column(name = "XTADRAUCL3_8")
    private Integer xtadraucl38;
    @Column(name = "XTADRAUCL3_9")
    private Integer xtadraucl39;
    @Column(name = "AASREF_0")
    private String aasref;
    @Column(name = "XDEPOSIT_0")
    private Integer xdeposit;
    @Column(name = "XSIDEOPE_0")
    private Integer xsideope;
    @Column(name = "LONGUEUR_0")
    private Integer longueur;
    @Column(name = "LARGEUR_0")
    private Integer largeur;
    @Column(name = "XSERIL_0")
    private Integer xseril;
    @Column(name = "XSERMGTCOD_0")
    private Integer xsermgtcod;
    @Column(name = "XLOTMGTCOD_0")
    private Integer xlotmgtcod;
    @Column(name = "XSTOMGTCOD_0")
    private Integer xstomgtcod;
    @Column(name = "XRENTABLE_0")
    private Integer xrentable;
    @Column(name = "XGNDOCC_0")
    private Double xgndocc;
    @Column(name = "XACCCOD_0")
    private String xacccod;
    @Column(name = "XSALESUNIT_0")
    private String xsalesunit;
    @Column(name = "XTAILGATE_0")
    private Integer xtailgate;
    @Column(name = "STYZON_0")
    private String styzon;
    @Column(name = "XUVYCOD_0")
    private String xuvycod;
    @Column(name = "XBATHGHT_0")
    private Double xbathght;
    @Column(name = "XINSPTYP_0")
    private String xinsptyp0;
    @Column(name = "XINSPTYP_1")
    private String xinsptyp1;
    @Column(name = "XLSTCHK_0")
    private Date xlstchk0;
    @Column(name = "XLSTCHK_1")
    private Date xlstchk1;
    @Column(name = "XPERIODICITY_0")
    private Integer xperiodicity0;
    @Column(name = "XPERIODICITY_1")
    private Integer xperiodicity1;
    @Column(name = "XNEXTVISIT_0")
    private Date xnextvisit0;
    @Column(name = "XNEXTVISIT_1")
    private Date xnextvisit1;
    @Column(name = "XTYPEIN_0")
    private Integer xtypein0;
    @Column(name = "XTYPEIN_1")
    private Integer xtypein1;


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
