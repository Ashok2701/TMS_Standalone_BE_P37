package com.transport.tms.Sync.Product.Service;

import com.transport.tms.Sync.Dto.SyncResult;
import com.transport.tms.Sync.Product.Entity.XRProduct;
import com.transport.tms.Sync.Product.Repository.ProductRepository;
import com.transport.tms.Sync.X3.Dto.X3ProductDTO;
import com.transport.tms.Sync.X3.Repository.X3ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSyncService {

    private final X3ProductRepository x3Repository;

    private final ProductRepository productRepository;

    @Transactional
    public SyncResult sync() {

        System.out.println("======================");
        System.out.println("PRODUCT SYNC STARTED");
        System.out.println("======================");

        Integer x3Count = x3Repository.count();
        System.out.println("X3 count = " + x3Count);

        Integer before = (int) productRepository.count();

        // Load all existing products into map — single DB query
        Map<String, XRProduct> existingMap =
                productRepository.findAll()
                        .stream()
                        .collect(Collectors.toMap(
                                XRProduct::getProductCode,
                                p -> p
                        ));

        List<X3ProductDTO> products = x3Repository.findProducts();
        System.out.println("X3 PRODUCTS FETCHED = " + products.size());

        int inserted = 0;
        int updated  = 0;
        int skipped  = 0;

        for (X3ProductDTO dto : products) {

            XRProduct existing = existingMap.get(dto.getProductCode());

            if (existing == null) {

                // NEW — insert
                XRProduct product = new XRProduct();
                mapX3ToEntity(dto, product);
                productRepository.save(product);
                inserted++;

            } else if (hasChanged(dto, existing)) {

                // CHANGED — update
                mapX3ToEntity(dto, existing);
                productRepository.save(existing);
                updated++;

            } else {

                // UNCHANGED — skip
                skipped++;
            }
        }

        Integer after = (int) productRepository.count();

        System.out.println("PRODUCT SYNC DONE — inserted=" + inserted
                + " updated=" + updated
                + " skipped=" + skipped);

        return new SyncResult(x3Count, before, after, inserted, updated, 0);
    }

    private void mapX3ToEntity(X3ProductDTO dto, XRProduct product) {

        product.setProductCode(dto.getProductCode());
        product.setProductName(dto.getProductName());
        product.setShortDescription(dto.getShortDescription());
        product.setProductCategory(dto.getProductCategory());
        product.setUnitOfMeasure(dto.getUnitOfMeasure());
        product.setSalesUnit(dto.getSalesUnit());
        product.setNetWeight(dto.getNetWeight());
        product.setGrossWeight(dto.getGrossWeight());
        product.setVolume(dto.getVolume());
        product.setWeightUnit(dto.getWeightUnit());
        product.setVolumeUnit(dto.getVolumeUnit());
        product.setActive(dto.getActive());
        product.setSyncedAt(LocalDateTime.now());
    }

    private boolean hasChanged(X3ProductDTO dto, XRProduct existing) {

        return !eq(dto.getProductName(),      existing.getProductName())
            || !eq(dto.getShortDescription(), existing.getShortDescription())
            || !eq(dto.getProductCategory(),  existing.getProductCategory())
            || !eq(dto.getUnitOfMeasure(),    existing.getUnitOfMeasure())
            || !eq(dto.getSalesUnit(),        existing.getSalesUnit())
            || !eq(dto.getWeightUnit(),       existing.getWeightUnit())
            || !eq(dto.getVolumeUnit(),       existing.getVolumeUnit())
            || !Objects.equals(dto.getNetWeight(),   existing.getNetWeight())
            || !Objects.equals(dto.getGrossWeight(), existing.getGrossWeight())
            || !Objects.equals(dto.getVolume(),      existing.getVolume())
            || !Objects.equals(dto.getActive(),      existing.getActive());
    }

    private boolean eq(String a, String b) {
        return Objects.equals(
                a == null ? "" : a.trim(),
                b == null ? "" : b.trim()
        );
    }
}
