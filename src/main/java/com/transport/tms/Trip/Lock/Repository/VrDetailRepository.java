package com.transport.tms.Trip.Lock.Repository;

import com.transport.tms.Trip.Lock.Entity.VrDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VrDetailRepository extends JpaRepository<VrDetail, UUID> {

    List<VrDetail> findByTripCodeOrderBySeqAsc(String tripCode);

    void deleteByTripCode(String tripCode);
}
