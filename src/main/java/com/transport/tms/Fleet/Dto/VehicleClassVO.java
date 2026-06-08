package com.transport.tms.Fleet.Dto;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class VehicleClassVO {
    private Long rowid;
    private String className;
    private String desc;
    private int enaFlag;
    private String cry;
    private int typ;
    private int axlnbr;
    private Double  xmaxcapw;
    private Double xmaxcapv;
    private Date createDateTime;
    private Date updateDateTime;
//    private byte[] auuid;
    private UUID auuid;
    private String creusr;
    private String updusr;
    private String xmaxvunit;
    private String xmaxunit;
    private int xskillno;
    private String xinspin;
    private int xmanin;
    private String xinspout;
    private int xmanout;
    private List<VehicleAssociation> associationList;
    private byte[] image;
}
