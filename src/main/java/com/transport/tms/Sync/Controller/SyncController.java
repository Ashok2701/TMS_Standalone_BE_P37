package com.transport.tms.Sync.Controller;


import com.transport.tms.Sync.Dto.SyncStatusDTO;
import com.transport.tms.Sync.Entity.XRSyncHistory;
import com.transport.tms.Sync.Service.SyncService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {


    private final SyncService service;



    @GetMapping("/status")
    public List<SyncStatusDTO> status(){

        return service.getStatus();

    }



    @GetMapping("/logs/{objectCode}")
    public List<XRSyncHistory> logs(
            @PathVariable String objectCode){

        return service.getLogs(
                objectCode);

    }



    @PostMapping("/{objectCode}")
    public void sync(
            @PathVariable String objectCode){

        service.syncObject(
                objectCode);

    }



    @PostMapping("/all")
    public void syncAll(){

        service.syncAll();

    }


}