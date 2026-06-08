package com.transport.tms.Sync.Product.Controller;

import com.transport.tms.Sync.Product.Entity.XRProduct;
import com.transport.tms.Sync.Product.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository repository;

    @GetMapping
    public List<XRProduct> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{productCode}")
    public XRProduct getById(
            @PathVariable String productCode) {

        return repository.findById(productCode)
                .orElseThrow(() ->
                        new RuntimeException("Product not found: " + productCode));
    }
}
