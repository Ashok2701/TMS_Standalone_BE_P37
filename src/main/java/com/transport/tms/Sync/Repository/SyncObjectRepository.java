package com.transport.tms.Sync.Repository;


import com.transport.tms.Sync.Entity.XRSyncObject;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SyncObjectRepository
        extends JpaRepository<XRSyncObject,String> {


}