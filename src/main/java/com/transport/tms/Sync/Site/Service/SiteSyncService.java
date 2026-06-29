package com.transport.tms.Sync.Site.Service;

import com.transport.tms.Sync.Dto.SyncResult;
import com.transport.tms.Sync.Site.Entity.XRSite;
import com.transport.tms.Sync.Site.Repository.SiteRepository;
import com.transport.tms.Sync.X3.Dto.X3SiteDTO;
import com.transport.tms.Sync.X3.Repository.X3SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteSyncService {

    private final X3SiteRepository x3Repository;

    private final SiteRepository repository;

    @Transactional
    public SyncResult sync() {

        System.out.println("======================");
        System.out.println("SITE SYNC STARTED");
        System.out.println("======================");

        Integer x3Count = x3Repository.count();
        System.out.println("X3 count = " + x3Count);

        Integer before = (int) repository.count();

        // Load ALL existing postgres sites into a map once — avoids N individual DB hits
        Map<String, XRSite> existingMap =
                repository.findAll()
                        .stream()
                        .collect(Collectors.toMap(
                                XRSite::getSiteCode,
                                s -> s
                        ));

        List<X3SiteDTO> sites = x3Repository.findSites();
        System.out.println("X3 SITES FETCHED = " + sites.size());

        int inserted = 0;
        int updated  = 0;
        int skipped  = 0;
        int failed   = 0;

        for (X3SiteDTO dto : sites) {

            try {

                XRSite existing = existingMap.get(dto.getSiteCode());

                if (existing == null) {

                    // ── NEW SITE ── insert
                    XRSite site = new XRSite();
                    site.setCreatedAt(LocalDateTime.now());
                    mapX3ToEntity(dto, site);
                    repository.save(site);
                    inserted++;
                    System.out.println("INSERTED site: " + dto.getSiteCode());

                } else if (hasChanged(dto, existing)) {

                    // ── CHANGED SITE ── update only changed fields
                    mapX3ToEntity(dto, existing);
                    repository.save(existing);
                    updated++;
                    System.out.println("UPDATED site: " + dto.getSiteCode());

                } else {

                    // ── UNCHANGED ── skip entirely
                    skipped++;
                }

            } catch (Exception e) {

                failed++;
                System.out.println(
                        "SITE FAILED : " + dto.getSiteCode()
                                + " ERROR : " + e.getMessage());
                e.printStackTrace();
            }
        }

        // ── DEACTIVATE sites no longer in X3 ────────────────
        java.util.Set<String> x3Codes = sites.stream()
                .map(X3SiteDTO::getSiteCode)
                .collect(java.util.stream.Collectors.toSet());

        int deactivated = 0;
        for (Map.Entry<String, XRSite> entry : existingMap.entrySet()) {
            if (!x3Codes.contains(entry.getKey())) {
                XRSite gone = entry.getValue();
                if (!Boolean.FALSE.equals(gone.getActive())) {
                    gone.setActive(false);
                    gone.setSyncedAt(java.time.LocalDateTime.now());
                    repository.save(gone);
                    deactivated++;
                    System.out.println("DEACTIVATED site: " + gone.getSiteCode());
                }
            }
        }

        Integer after = (int) repository.count();

        System.out.println("SYNC DONE — inserted=" + inserted
                + " updated=" + updated
                + " skipped=" + skipped
                + " failed=" + failed);

        return new SyncResult(x3Count, before, after, inserted, updated, failed);
    }

    // ── Map X3 fields → entity (only X3-owned fields, never touch TMS fields) ──
    private void mapX3ToEntity(X3SiteDTO dto, XRSite site) {

        site.setSiteCode(dto.getSiteCode());
        site.setSiteName(dto.getSiteName());
        site.setShortName(dto.getShortName());
        site.setCountryCode(dto.getCountryCode());
        site.setAddressCode(dto.getAddressCode());
        site.setAddressDescription(dto.getAddressDescription());
        site.setAddressLine1(dto.getAddressLine1());
        site.setAddressLine2(dto.getAddressLine2());
        site.setAddressLine3(dto.getAddressLine3());
        site.setPostalCode(dto.getPostalCode());
        site.setCity(dto.getCity());
        site.setStateCode(dto.getStateCode());
        site.setCountryName(dto.getCountryName());
        site.setSyncedAt(LocalDateTime.now());
    }

    // ── Compare X3 data vs existing Postgres row ──
    // Returns true only if at least one X3-owned field differs
    private boolean hasChanged(X3SiteDTO dto, XRSite existing) {

        return !eq(dto.getSiteName(),          existing.getSiteName())
            || !eq(dto.getShortName(),         existing.getShortName())
            || !eq(dto.getCountryCode(),       existing.getCountryCode())
            || !eq(dto.getAddressCode(),       existing.getAddressCode())
            || !eq(dto.getAddressDescription(),existing.getAddressDescription())
            || !eq(dto.getAddressLine1(),      existing.getAddressLine1())
            || !eq(dto.getAddressLine2(),      existing.getAddressLine2())
            || !eq(dto.getAddressLine3(),      existing.getAddressLine3())
            || !eq(dto.getPostalCode(),        existing.getPostalCode())
            || !eq(dto.getCity(),              existing.getCity())
            || !eq(dto.getStateCode(),         existing.getStateCode())
            || !eq(dto.getCountryName(),       existing.getCountryName());
    }

    private boolean eq(String a, String b) {
        return Objects.equals(
                a == null ? "" : a.trim(),
                b == null ? "" : b.trim()
        );
    }
}
