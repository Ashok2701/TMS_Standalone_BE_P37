package com.transport.tms.Pod.Controller;

import com.transport.tms.Config.Anonymous;
import com.transport.tms.Pod.Dto.DriverLoginRequestDTO;
import com.transport.tms.Pod.Dto.DriverLoginResponseDTO;
import com.transport.tms.Pod.Service.CurrentDriver;
import com.transport.tms.Pod.Service.DriverAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pod/auth")
@RequiredArgsConstructor
@Slf4j
public class DriverAuthController {

    private final DriverAuthService authService;
    private final CurrentDriver currentDriver;

    @PostMapping("/login")
    @Anonymous
    public ResponseEntity<Object> login(@RequestBody DriverLoginRequestDTO dto) {

        try {
            DriverLoginResponseDTO result = authService.login(dto);
            return ResponseEntity.ok(result);

        } catch (RuntimeException ex) {
            log.error("Driver login failed for username={}", dto.getUsername(), ex);

            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    /** Requires a valid driver token — resolves which driver is logging
     *  out from the token itself (via CurrentDriver), never from a
     *  request parameter, so one driver's token can't be used to close
     *  another driver's session. */
    @PostMapping("/logout")
    public ResponseEntity<Object> logout() {
        try {
            String driverId = currentDriver.require().getDriverId();
            authService.logout(driverId);
            return ResponseEntity.ok(Map.of("message", "Logged out"));

        } catch (RuntimeException ex) {
            log.error("Driver logout failed: {}", ex.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }
}
