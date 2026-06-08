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

@Service
@RequiredArgsConstructor
public class ProductSyncService {

    private final X3ProductRepository x3Repository;

    private final ProductRepository productRepository;

    @Transactional
    public SyncResult sync() {

        Integer x3Count =
                x3Repository.count();

        Integer before =
                (int) productRepository.count();

        List<X3ProductDTO> products =
                x3Repository.findProducts();

        int inserted = 0;
        int updated  = 0;

        for (X3ProductDTO dto : products) {

            boolean exists =
                    productRepository.existsById(
                            dto.getProductCode());

            XRProduct product =
                    productRepository
                            .findById(dto.getProductCode())
                            .orElse(new XRProduct());

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

            productRepository.save(product);

            if (exists) updated++;
            else         inserted++;
        }

        Integer after =
                (int) productRepository.count();

        return new SyncResult(
                x3Count, before, after, inserted, updated, 0);
    }
}
