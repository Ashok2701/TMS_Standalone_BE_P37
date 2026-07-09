package com.transport.tms.transport.sqlserver.Service;

import com.transport.tms.Sync.Site.Entity.XRSite;
import com.transport.tms.RoutePlanner.Repository.RoutePlannerSiteRepository;
import com.transport.tms.transport.sqlserver.Dto.SiteDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Legacy transport/sites endpoint — now served from Postgres xr_site
 * (previously queried X3 TMSNEW.XTMSUSRFCY which may not exist)
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransportService {

    private final RoutePlannerSiteRepository siteRepository;

    public List<SiteDTO> getSites() {
        return siteRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .map(this::toDTO)
                .toList();
    }

    private SiteDTO toDTO(XRSite s) {
        SiteDTO dto = new SiteDTO();
        dto.setFcy(s.getSiteCode());
        dto.setFcynam(s.getSiteName());
        dto.setCry(s.getCountryCode());
        dto.setXx10c_geox(s.getLatitude()  != null ? s.getLatitude().toPlainString()  : null);
        dto.setXx10c_geoy(s.getLongitude() != null ? s.getLongitude().toPlainString() : null);
        return dto;
    }
}
