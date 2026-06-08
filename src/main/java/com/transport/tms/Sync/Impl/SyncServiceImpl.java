package com.transport.tms.Sync.Impl;

import com.transport.tms.Sync.Customer.Service.CustomerAddressSyncService;
import com.transport.tms.Sync.Customer.Service.CustomerSyncService;
import com.transport.tms.Sync.Dto.SyncResult;
import com.transport.tms.Sync.Dto.SyncStatusDTO;
import com.transport.tms.Sync.Entity.XRSyncHistory;
import com.transport.tms.Sync.Entity.XRSyncObject;
import com.transport.tms.Sync.Product.Service.ProductSyncService;
import com.transport.tms.Sync.Repository.SyncHistoryRepository;
import com.transport.tms.Sync.Repository.SyncObjectRepository;
import com.transport.tms.Sync.Service.SyncService;
import com.transport.tms.Sync.Site.Service.SiteSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    private final SyncObjectRepository objectRepository;

    private final SyncHistoryRepository historyRepository;

    private final CustomerSyncService customerSyncService;

    private final CustomerAddressSyncService customerAddressSyncService;

    private final SiteSyncService siteSyncService;

    private final ProductSyncService productSyncService;

    // =====================================
    // DASHBOARD STATUS
    // =====================================

    @Override
    public List<SyncStatusDTO> getStatus() {

        return objectRepository
                .findAll()
                .stream()
                .map(object -> {

                    SyncStatusDTO dto = new SyncStatusDTO();

                    dto.setObjectCode(object.getObjectCode());
                    dto.setObjectName(object.getObjectName());
                    dto.setX3Count(object.getX3Count());
                    dto.setPostgresCount(object.getPostgresCount());
                    dto.setDifferenceCount(object.getDifferenceCount());
                    dto.setStatus(object.getStatus());
                    dto.setLastSyncTime(object.getLastSyncTime());

                    return dto;
                })
                .toList();
    }

    // =====================================
    // LOGS
    // =====================================

    @Override
    public List<XRSyncHistory> getLogs(String objectCode) {

        return historyRepository
                .findByObjectCodeOrderByStartedAtDesc(objectCode);
    }

    // =====================================
    // SINGLE OBJECT SYNC
    // =====================================

    @Override
    public void syncObject(String objectCode) {

        XRSyncHistory history = new XRSyncHistory();
        history.setObjectCode(objectCode);
        history.setStartedAt(LocalDateTime.now());

        try {

            SyncResult result;

            switch (objectCode.toUpperCase()) {

                case "CUSTOMER" ->
                        result = customerSyncService.sync();

                case "CUSTOMER_ADDRESS" ->
                        result = customerAddressSyncService.sync();

                case "SITE" ->
                        result = siteSyncService.sync();

                case "PRODUCT" ->
                        result = productSyncService.sync();

                default ->
                        throw new RuntimeException(
                                "Invalid sync object : " + objectCode);
            }

            // update sync dashboard table

            XRSyncObject syncObject =
                    objectRepository
                            .findById(objectCode.toUpperCase())
                            .orElseThrow(() ->
                                    new RuntimeException("Sync object missing"));

            syncObject.setX3Count(result.getX3Count());
            syncObject.setPostgresCount(result.getAfterCount());
            syncObject.setDifferenceCount(
                    result.getX3Count() - result.getAfterCount());
            syncObject.setStatus(
                    result.getFailed() == 0 ? "SUCCESS" : "PARTIAL");
            syncObject.setLastSyncTime(LocalDateTime.now());

            objectRepository.save(syncObject);

            // insert history

            history.setCompletedAt(LocalDateTime.now());
            history.setX3Count(result.getX3Count());
            history.setPostgresBeforeCount(result.getBeforeCount());
            history.setPostgresAfterCount(result.getAfterCount());
            history.setInsertedCount(result.getInserted());
            history.setUpdatedCount(result.getUpdated());
            history.setFailedCount(result.getFailed());
            history.setStatus(syncObject.getStatus());

        } catch (Exception e) {

            history.setCompletedAt(LocalDateTime.now());
            e.printStackTrace();
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
        }

        historyRepository.save(history);
    }

    // =====================================
    // SYNC ALL
    // =====================================

    @Override
    public void syncAll() {

        objectRepository
                .findAll()
                .forEach(object ->
                        syncObject(object.getObjectCode()));
    }
}
