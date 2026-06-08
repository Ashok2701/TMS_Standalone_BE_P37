package com.transport.tms.Sync.Product.Controller;

import com.transport.tms.Sync.Product.Dto.ProductTmsDTO;
import com.transport.tms.Sync.Product.Service.ProductTmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{productCode}/tms")
@RequiredArgsConstructor
public class ProductTmsController {

    private final ProductTmsService service;

    @GetMapping
    public ProductTmsDTO get(
            @PathVariable String productCode) {

        return service.getTmsFields(productCode);
    }

    @PutMapping
    public ProductTmsDTO update(
            @PathVariable String productCode,
            @RequestBody ProductTmsDTO dto) {

        return service.updateTmsFields(productCode, dto);
    }
}
