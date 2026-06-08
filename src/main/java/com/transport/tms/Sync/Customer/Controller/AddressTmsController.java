package com.transport.tms.Sync.Customer.Controller;

import com.transport.tms.Sync.Customer.Dto.AddressTmsDTO;
import com.transport.tms.Sync.Customer.Service.AddressTmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer-addresses/{customerCode}/{addressCode}/tms")
@RequiredArgsConstructor
public class AddressTmsController {

    private final AddressTmsService service;

    @GetMapping
    public AddressTmsDTO get(
            @PathVariable String customerCode,
            @PathVariable String addressCode) {

        return service.getTmsData(customerCode, addressCode);
    }

    @PutMapping
    public AddressTmsDTO save(
            @PathVariable String customerCode,
            @PathVariable String addressCode,
            @RequestBody AddressTmsDTO dto) {

        return service.saveTmsData(customerCode, addressCode, dto);
    }
}
