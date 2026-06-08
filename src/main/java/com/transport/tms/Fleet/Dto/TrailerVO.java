package com.transport.tms.Fleet.Dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TrailerVO {
    private Long rowid;
    private String trailer;
    private String des;
    private String type;
    private String linkTo;
    private Double maxLen;
    private Double maxWid;
    private Double maxFH;
    private Double maxLovol;
    private Double maxLoams;
    private Double curbWei;
    private Double gvwr;
    private Double nbaxle;
    private String make = "";
    private String model = "";
    private int annee;
    private Date insCheck;
    private Date lastInsp;
    private String comment;
    private Date creatDateTime;
    private Date updateDateTime;
    private String creUser;
    private String updUser;
    private String fcy;
    private String xmaxlovol;
    private String xmaxloams;
    private Integer xtrktyp;
    private String xtracpy;
    private String xadrcer;
    private Integer xtrkisoa;
    private Date xtecdat;
    private Date xisoexpiry;
    private Date xadrdat;
    private Date xwprchkd;
    private Date xhydprchkd;
    private String aasref;
    private Integer xdeposit;
    private Integer xsideope;
    private Integer longueur;
    private Integer largeur;
    private Integer xseril;
    private Integer xsermgtcod;
    private Integer xlotmgtcod;
    private Integer xstomgtcod;
    private Integer xrentable;
    private Double xgndocc;
    private String xacccod;
    private String xsalesunit;
    private Integer xtailgate;
    private String styzon;
    private String xuvycod;
    private Double xbathght;
    private List<TrailerCompartment> compartmentList;
    private List<TechnicalInspection> technicalInspectionList;
    private byte[] image;
}
