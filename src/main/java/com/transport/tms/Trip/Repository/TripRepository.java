package com.transport.tms.Trip.Repository;

import com.transport.tms.Trip.Entity.XrTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<XrTrip, Long> {

    List<XrTrip> findBySiteAndDocDateOrderByCreateDateAsc(String site, LocalDate docDate);

    List<XrTrip> findBySiteOrderByDocDateDescCreateDateAsc(String site);

    boolean existsByTripCode(String tripCode);

    @Query("SELECT COALESCE(MAX(t.startIndex), 0) FROM XrTrip t WHERE t.site = :site AND t.docDate = :docDate")
    int findMaxStartIndex(@Param("site") String site, @Param("docDate") LocalDate docDate);

    @Query("SELECT t FROM XrTrip t WHERE t.site = :site AND t.docDate = :docDate AND t.status = :status")
    List<XrTrip> findBySiteAndDocDateAndStatus(@Param("site") String site,
                                                @Param("docDate") LocalDate docDate,
                                                @Param("status") String status);
}
