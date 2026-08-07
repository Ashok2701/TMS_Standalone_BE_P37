package com.transport.tms.Pod.Service;

import com.transport.tms.Fleet.Entity.Driver;
import com.transport.tms.Fleet.Repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves "which driver is making this request" from the per-request
 * SecurityContext populated by JwtAuthenticationFilter. Every future POD
 * endpoint that needs to scope data to the authenticated driver (their
 * trips, their stops, their POD submissions) should go through this
 * rather than trusting a driverId passed in the request body/query —
 * that would let one driver's token be used to read or act on another
 * driver's data just by changing a parameter.
 */
@Component
@RequiredArgsConstructor
public class CurrentDriver {

    private final DriverRepository driverRepository;

    /** Throws if there's no authenticated driver on this request. */
    public Driver require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new RuntimeException("Not authenticated");
        }
        boolean isDriverToken = auth.getAuthorities().stream()
                .anyMatch(a -> "DRIVER".equals(a.getAuthority()));
        if (!isDriverToken) {
            throw new RuntimeException("Not a driver token");
        }
        return driverRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Driver not found for authenticated user"));
    }
}
