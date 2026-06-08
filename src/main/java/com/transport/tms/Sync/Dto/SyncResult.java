package com.transport.tms.Sync.Dto;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class SyncResult {


    private Integer x3Count;


    private Integer beforeCount;


    private Integer afterCount;


    private Integer inserted;


    private Integer updated;


    private Integer failed;


}