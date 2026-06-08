package com.transport.tms.Sync.Product.Service;

import com.transport.tms.Sync.Product.Dto.ProductTmsDTO;
import com.transport.tms.Sync.Product.Entity.XRProduct;
import com.transport.tms.Sync.Product.Repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductTmsService {

    private final ProductRepository repository;

    // ── GET TMS fields for a product ───────────────────────────
    public ProductTmsDTO getTmsFields(String productCode) {

        XRProduct product = repository.findById(productCode)
                .orElseThrow(() ->
                        new RuntimeException("Product not found: " + productCode));

        ProductTmsDTO dto = new ProductTmsDTO();
        dto.setServiceTime(product.getServiceTime());

        return dto;
    }

    // ── UPDATE TMS fields only ──────────────────────────────────
    @Transactional
    public ProductTmsDTO updateTmsFields(String productCode,
                                         ProductTmsDTO dto) {

        XRProduct product = repository.findById(productCode)
                .orElseThrow(() ->
                        new RuntimeException("Product not found: " + productCode));

        // Only TMS fields — never touch X3 fields
        product.setServiceTime(dto.getServiceTime());
        product.setUpdatedBy(dto.getUpdatedBy());
        product.setUpdatedAt(LocalDateTime.now());

        repository.save(product);

        return getTmsFields(productCode);
    }
}
