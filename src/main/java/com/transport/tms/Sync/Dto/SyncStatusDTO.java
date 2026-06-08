package com.transport.tms.Sync.Dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class SyncStatusDTO {


    private String objectCode;


    private String objectName;


    private Integer x3Count;


    private Integer postgresCount;


    private Integer differenceCount;


    private String status;


    private LocalDateTime lastSyncTime;

}