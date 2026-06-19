package com.transport.tms.RoutePlanner.Repository;

import com.transport.tms.Sync.Site.Entity.XRSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoutePlannerSiteRepository
        extends JpaRepository<XRSite, String> {

    @Query("SELECT s FROM XRSite s WHERE s.tmsFlag = true")
    List<XRSite> findAllTmsSites();

    @Query("SELECT s FROM XRSite s WHERE s.siteCode = :siteCode AND s.tmsFlag = true")
    Optional<XRSite> findTmsSiteBySiteCode(@Param("siteCode") String siteCode);
}
