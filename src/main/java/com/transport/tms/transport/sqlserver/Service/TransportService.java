package com.transport.tms.transport.sqlserver.Service;


import com.transport.tms.transport.sqlserver.Dto.SiteDTO;
import com.transport.tms.transport.sqlserver.Entity.sites;
import com.transport.tms.transport.sqlserver.repository.SiteX3Repository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class TransportService {

    @Autowired
    private SiteX3Repository siteX3Repository;

    public List<SiteDTO> getSites() {
        List<sites> sitesdata = siteX3Repository.findAll();
        List<SiteDTO> list = new ArrayList<>();
        for (sites eachsite : sitesdata) {
            try {
                SiteDTO eachDTO = new SiteDTO();
                eachDTO.setFcy(eachsite.getFcy());
                eachDTO.setFcynam(eachsite.getFcynam());
                eachDTO.setCry(eachsite.getCry());
                eachDTO.setXx10c_geox(eachsite.getXx10c_geox());
                eachDTO.setXx10c_geoy(eachsite.getXx10c_geoy());
                eachDTO.setFcyNumber(eachsite.getFcyNumber());

                list.add(eachDTO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
