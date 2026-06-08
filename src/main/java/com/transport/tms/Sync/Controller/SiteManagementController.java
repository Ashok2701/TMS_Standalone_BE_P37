package com.transport.tms.Sync.Site.Controller;


import com.transport.tms.Sync.Site.Dto.SiteUpdateDTO;

import com.transport.tms.Sync.Site.Entity.XRSite;

import com.transport.tms.Sync.Site.Service.SiteManagementService;


import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.*;


import java.util.List;



@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteManagementController {



    private final SiteManagementService service;



    @GetMapping
    public List<XRSite> getSites(){


        return service.getAllSites();

    }



    @GetMapping("/{siteCode}")
    public XRSite getSite(

            @PathVariable String siteCode){


        return service.getSite(
                siteCode
        );

    }






    @PutMapping("/{siteCode}")
    public XRSite updateSite(

            @PathVariable String siteCode,

            @RequestBody SiteUpdateDTO dto){


        return service.updateSite(
                siteCode,
                dto
        );

    }



}