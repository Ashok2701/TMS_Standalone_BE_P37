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



@Service
@RequiredArgsConstructor
public class SiteSyncService {



    private final X3SiteRepository x3Repository;


    private final SiteRepository repository;




    @Transactional
    public SyncResult sync(){

        System.out.println("======================");
        System.out.println("SITE SYNC STARTED");
        System.out.println("======================");

        Integer x3Count =
                x3Repository.count();

        System.out.println("X3 count"+ x3Count);

        Integer before =
                (int) repository.count();



        List<X3SiteDTO> sites =
                x3Repository.findSites();


        System.out.println(
                "X3 SITES FETCHED = "
                        + sites.size()
        );

        int inserted=0;

        int updated=0;

        int failed=0;




        for(X3SiteDTO dto: sites){


            try{


                boolean exists =
                        repository.existsById(
                                dto.getSiteCode()
                        );



                boolean isNew = false;


                XRSite site =
                        repository
                                .findById(dto.getSiteCode())
                                .orElse(null);


                if(site == null){

                    site = new XRSite();

                    site.setCreatedAt(
                            LocalDateTime.now()
                    );

                    isNew = true;

                }




                site.setSiteCode(
                        dto.getSiteCode());


                site.setSiteName(
                        dto.getSiteName());


                site.setShortName(
                        dto.getShortName());


                site.setCountryCode(
                        dto.getCountryCode());


                site.setSyncedAt(
                        LocalDateTime.now());

                site.setAddressCode(
                        dto.getAddressCode());

                site.setAddressDescription(
                        dto.getAddressDescription());

                site.setAddressLine1(
                        dto.getAddressLine1());

                site.setAddressLine2(
                        dto.getAddressLine2());

                site.setAddressLine3(
                        dto.getAddressLine3());

                site.setPostalCode(
                        dto.getPostalCode());

                site.setCity(
                        dto.getCity());

                site.setStateCode(
                        dto.getStateCode());

                site.setCountryName(
                        dto.getCountryName());

                System.out.println(
                        "Saving Site : "
                                + site.getSiteCode()
                );

                repository.save(site);



                if(exists)

                    updated++;

                else

                    inserted++;



            }catch(Exception e){


                failed++;


                System.out.println(
                        "SITE FAILED : "
                                + dto.getSiteCode()
                                + " ERROR : "
                                + e.getMessage()
                );


                e.printStackTrace();


            }

        }



        Integer after =
                (int)repository.count();




        return new SyncResult(

                x3Count,

                before,

                after,

                inserted,

                updated,

                failed

        );



    }



}