package com.transport.tms.Trip.Lock.Repository;

import com.transport.tms.Trip.Lock.Entity.LvsHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LvsHeaderRepository extends JpaRepository<LvsHeader, String> {

    Optional<LvsHeader> findByTripCode(String tripCode);

    /** For LVS number sequencing — same prefix-matching approach as before. */
    List<LvsHeader> findByLvsNumberStartingWithOrderByLvsNumberDesc(String prefix);
}
