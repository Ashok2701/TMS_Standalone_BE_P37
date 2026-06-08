package com.transport.tms.Sync.Customer.Controller;

import com.transport.tms.Sync.Customer.Dto.CustomerTmsDTO;
import com.transport.tms.Sync.Customer.Service.CustomerTmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers/{customerCode}/tms")
@RequiredArgsConstructor
public class CustomerTmsController {

    private final CustomerTmsService service;

    @GetMapping
    public CustomerTmsDTO get(
            @PathVariable String customerCode) {

        return service.getTmsFields(customerCode);
    }

    @PutMapping
    public CustomerTmsDTO update(
            @PathVariable String customerCode,
            @RequestBody CustomerTmsDTO dto) {

        return service.updateTmsFields(customerCode, dto);
    }
}
