package com.transport.tms.Trip.Lock.Repository;

import com.transport.tms.Trip.Lock.Entity.VrHeader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VrHeaderRepository extends JpaRepository<VrHeader, String> {
}
