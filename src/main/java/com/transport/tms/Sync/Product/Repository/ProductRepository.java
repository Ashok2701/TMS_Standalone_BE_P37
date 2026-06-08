package com.transport.tms.Sync.Product.Repository;

import com.transport.tms.Sync.Product.Entity.XRProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository
        extends JpaRepository<XRProduct, String> {

}
