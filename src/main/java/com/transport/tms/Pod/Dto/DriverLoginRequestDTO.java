package com.transport.tms.Pod.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverLoginRequestDTO {

    private String username;

    private String password;

    /** A stable identifier for this device (however the app generates
     *  one) — used to tell "same device logging in again" apart from
     *  "a different device", for future force-logout/session review. */
    private String deviceId;

    /** Free-text device model/OS, e.g. "Pixel 8 / Android 15". */
    private String deviceModel;
}
