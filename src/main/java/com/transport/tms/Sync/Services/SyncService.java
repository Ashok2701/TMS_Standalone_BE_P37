package com.transport.tms.Sync.Service;


import com.transport.tms.Sync.Dto.SyncStatusDTO;
import com.transport.tms.Sync.Entity.XRSyncHistory;

import java.util.List;


public interface SyncService {


    List<SyncStatusDTO> getStatus();


    List<XRSyncHistory> getLogs(
            String objectCode);


    void syncObject(
            String objectCode);


    void syncAll();

}