package com.transport.tms.Fleet.Repository;


import com.transport.tms.Fleet.Entity.Allocation;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AllocationRepository extends CrudRepository<Allocation, Long> {
    Allocation findByTransactionNumber(String transactionNumber);
    List<Allocation> findAll();
    void deleteByTransactionNumber(String transactionNumber);
    boolean existsByTransactionNumber(String transactionNumber);
}
