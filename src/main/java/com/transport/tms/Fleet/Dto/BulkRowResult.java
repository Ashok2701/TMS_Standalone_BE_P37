package com.transport.tms.Fleet.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Per-row outcome for a bulk create-or-update import (Vehicle, Driver,
 * Vehicle Category). rowIndex matches the 0-based index of the row in the
 * request list, so the frontend can map results back onto its preview
 * table without needing a shared key.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkRowResult {
    private int rowIndex;
    private boolean success;
    private boolean isUpdate;
    private String key;     // vehicleCode / driverId / categoryCode
    private String error;   // null when success = true
}
