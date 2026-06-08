package com.transport.tms.Sync.Customer.Repository;


import com.transport.tms.Sync.Customer.Entity.XRCustomer;

import org.springframework.data.jpa.repository.JpaRepository;



public interface CustomerRepository

        extends JpaRepository<XRCustomer,String>{


}