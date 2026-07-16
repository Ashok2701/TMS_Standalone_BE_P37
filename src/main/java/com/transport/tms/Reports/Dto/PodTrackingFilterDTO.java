package com.transport.tms.Reports.Dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Incoming filter/payload for PodTracking, bound from query parameters via
 * @ModelAttribute (GET-only — no request body). Add filter fields here
 * as the frontend needs them (e.g. driverId, status, search text).
 */
@Data
public class PodTrackingFilterDTO {
    private LocalDate start;
    private LocalDate end;
    // TODO: add report-specific filter fields, e.g. private Long driverId;
}
