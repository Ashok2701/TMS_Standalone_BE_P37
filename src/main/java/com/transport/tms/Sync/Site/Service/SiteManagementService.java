package com.transport.tms.Sync.Site.Service;


import com.transport.tms.Sync.Site.Dto.SiteUpdateDTO;
import com.transport.tms.Sync.Site.Entity.XRSite;

import java.util.List;


public interface SiteManagementService {


    List<XRSite> getAllSites();


    XRSite getSite(
            String siteCode
    );


    XRSite updateSite(
            String siteCode,
            SiteUpdateDTO dto
    );


}