package com.transport.tms.transport.sqlserver.Controllers;

import com.transport.tms.transport.sqlserver.Dto.SiteDTO;
import com.transport.tms.transport.sqlserver.Service.TransportService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transport")
@Slf4j
public class TransportController {

    private static final Logger log = LoggerFactory.getLogger(TransportController.class);


    @Autowired
    private TransportService transportService;

    @GetMapping("/sites")
    public List<SiteDTO> getSites(){
        return transportService.getSites();
    }



}
