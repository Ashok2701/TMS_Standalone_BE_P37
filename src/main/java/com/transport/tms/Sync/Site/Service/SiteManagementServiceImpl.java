package com.transport.tms.Sync.Site.Service;


import com.transport.tms.Sync.Site.Dto.SiteUpdateDTO;
import com.transport.tms.Sync.Site.Entity.XRSite;
import com.transport.tms.Sync.Site.Repository.SiteRepository;


import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import java.util.List;



@Service
@RequiredArgsConstructor
public class SiteManagementServiceImpl
        implements SiteManagementService {



    private final SiteRepository repository;



    @Override
    public List<XRSite> getAllSites(){


        return repository.findAll();

    }




    @Override
    public XRSite getSite(
            String siteCode){


        return repository.findById(siteCode)

                .orElseThrow(() ->
                        new RuntimeException(
                                "Site not found"
                        )
                );

    }






    @Transactional
    @Override
    public XRSite updateSite(

            String siteCode,

            SiteUpdateDTO dto){



        XRSite site =
                repository.findById(
                                siteCode
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Site not found"
                                )
                        );



        // ONLY TMS FIELDS UPDATE


        site.setLatitude(
                dto.getLatitude()
        );


        site.setLongitude(
                dto.getLongitude()
        );


        site.setWorkingStartTime(
                dto.getWorkingStartTime()
        );


        site.setWorkingEndTime(
                dto.getWorkingEndTime()
        );


        site.setLoadingDockCount(
                dto.getLoadingDockCount()
        );


        site.setMaxVehicleCapacity(
                dto.getMaxVehicleCapacity()
        );


        site.setTmsFlag(
                dto.getTmsFlag()
        );


        site.setRemarks(
                dto.getRemarks()
        );


        site.setUpdatedBy(
                dto.getUpdatedBy()
        );


        site.setUpdatedAt(
                LocalDateTime.now()
        );



        return repository.save(site);

    }


}