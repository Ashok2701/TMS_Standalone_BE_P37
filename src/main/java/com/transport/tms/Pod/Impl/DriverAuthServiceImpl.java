package com.transport.tms.Pod.Impl;

import com.transport.tms.Fleet.Entity.Driver;
import com.transport.tms.Fleet.Repository.DriverRepository;
import com.transport.tms.Pod.Dto.DriverLoginRequestDTO;
import com.transport.tms.Pod.Dto.DriverLoginResponseDTO;
import com.transport.tms.Pod.Entity.DriverSession;
import com.transport.tms.Pod.Repository.DriverSessionRepository;
import com.transport.tms.Pod.Service.DriverAuthService;
import com.transport.tms.UserManagement.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// POD app driver authentication — checks credentials directly against
// xr_driver.username/password (a deliberate, separate credential store
// from xr_users/XRAuth, per instruction — not a shared login with the
// web app's admin/dispatcher accounts). Reuses the same TokenService
// (same JWT signing key/algorithm) so tokens are structurally
// consistent across both login paths, just with "DRIVER" as the
// authority instead of a role code.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DriverAuthServiceImpl implements DriverAuthService {

    private final DriverRepository driverRepository;
    private final DriverSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    @Transactional
    public DriverLoginResponseDTO login(DriverLoginRequestDTO dto) {

        if (dto.getUsername() == null || dto.getUsername().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("Username and password are required");
        }

        Driver driver = driverRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getPassword() == null || driver.getPassword().isBlank()) {
            throw new RuntimeException("No password set for this driver — contact an administrator");
        }

        if (!passwordEncoder.matches(dto.getPassword(), driver.getPassword())) {
            throw new RuntimeException("Password is wrong");
        }

        if (!Boolean.TRUE.equals(driver.getActive())) {
            throw new RuntimeException("Driver is inactive");
        }

        // Single active session enforcement — a driver already logged in
        // on some device must log out (or be force-logged-out by an
        // admin, once that's built) before they can log in elsewhere.
        if (sessionRepository.findByDriverIdAndActiveTrue(driver.getDriverId()).isPresent()) {
            throw new RuntimeException("Already logged in on another device");
        }

        String token = tokenService.generateAccessToken(
                List.of("DRIVER"),
                driver.getUsername());

        DriverSession session = new DriverSession();
        session.setDriverId(driver.getDriverId());
        session.setDeviceId(dto.getDeviceId());
        session.setDeviceModel(dto.getDeviceModel());
        session.setLoginAt(LocalDateTime.now());
        session.setActive(true);
        sessionRepository.save(session);

        return DriverLoginResponseDTO.builder()
                .accessToken(token)
                .driverId(driver.getDriverId())
                .driverName(driver.getDriverName())
                .username(driver.getUsername())
                .site(driver.getSite())
                .mobileNo(driver.getMobileNo())
                .build();
    }

    @Override
    @Transactional
    public void logout(String driverId) {
        DriverSession session = sessionRepository.findByDriverIdAndActiveTrue(driverId)
                .orElseThrow(() -> new RuntimeException("No active session found for this driver"));
        session.setLogoutAt(LocalDateTime.now());
        session.setActive(false);
        sessionRepository.save(session);
    }
}
