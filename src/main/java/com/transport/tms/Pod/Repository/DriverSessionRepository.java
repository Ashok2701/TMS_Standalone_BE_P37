package com.transport.tms.Pod.Repository;

import com.transport.tms.Pod.Entity.DriverSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverSessionRepository extends JpaRepository<DriverSession, UUID> {

    Optional<DriverSession> findByDriverIdAndActiveTrue(String driverId);
}
