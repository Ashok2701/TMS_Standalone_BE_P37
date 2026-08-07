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
    java.util.Optional<XrTrip> findByTripCode(String tripCode);

    @Query("SELECT COALESCE(MAX(t.startIndex), 0) FROM XrTrip t WHERE t.site = :site AND t.docDate = :docDate")
    int findMaxStartIndex(@Param("site") String site, @Param("docDate") LocalDate docDate);

    @Query("SELECT t FROM XrTrip t WHERE t.site = :site AND t.docDate = :docDate AND t.optiStatus = :status")
    List<XrTrip> findBySiteAndDocDateAndStatus(@Param("site") String site,
                                                @Param("docDate") LocalDate docDate,
                                                @Param("status") String status);

    // POD: a driver's trips for a given date. Not filtered by site — a
    // driver only ever has trips for whatever site(s) they were actually
    // assigned to, no need to also know/pass which site here.
    List<XrTrip> findByDriverIdAndDocDateOrderByStartTimeAsc(String driverId, LocalDate docDate);

    // POD: all of a driver's trips, used to locate which trip a given
    // docNum belongs to (stops are embedded JSON per trip, not a
    // separate queryable table) when the caller only has a docNum, not
    // a tripCode — e.g. GET /api/pod/stops/{docNum}.
    List<XrTrip> findByDriverId(String driverId);
}
