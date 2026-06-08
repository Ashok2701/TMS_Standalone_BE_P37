package com.transport.tms.Sync.Repository;


import com.transport.tms.Sync.Entity.XRSyncHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface SyncHistoryRepository
        extends JpaRepository<XRSyncHistory, UUID> {


    List<XRSyncHistory>
    findByObjectCodeOrderByStartedAtDesc(
            String objectCode
    );

}