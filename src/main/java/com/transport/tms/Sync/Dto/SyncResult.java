package com.transport.tms.Sync.Dto;

import lombok.Getter;

@Getter
public class SyncResult {

    private final Integer x3Count;
    private final Integer beforeCount;
    private final Integer afterCount;
    private final Integer inserted;
    private final Integer updated;
    private final Integer failed;
    private final Integer deactivated;

    // Constructor with deactivated (new)
    public SyncResult(Integer x3Count, Integer beforeCount, Integer afterCount,
                      Integer inserted, Integer updated, Integer failed, Integer deactivated) {
        this.x3Count      = x3Count;
        this.beforeCount  = beforeCount;
        this.afterCount   = afterCount;
        this.inserted     = inserted;
        this.updated      = updated;
        this.failed       = failed;
        this.deactivated  = deactivated;
    }

    // Backward-compatible constructor (deactivated defaults to 0)
    public SyncResult(Integer x3Count, Integer beforeCount, Integer afterCount,
                      Integer inserted, Integer updated, Integer failed) {
        this(x3Count, beforeCount, afterCount, inserted, updated, failed, 0);
    }
}
