package com.transport.tms.Sync.Customer.Service;


import com.transport.tms.Sync.Customer.Entity.XRCustomer;

import com.transport.tms.Sync.Customer.Repository.CustomerRepository;

import com.transport.tms.Sync.Dto.SyncResult;

import com.transport.tms.Sync.X3.Dto.X3CustomerDTO;

import com.transport.tms.Sync.X3.Repository.X3CustomerRepository;


import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CustomerSyncService {


    private final X3CustomerRepository x3Repository;


    private final CustomerRepository customerRepository;



    @Transactional
    public SyncResult sync(){



        Integer x3Count =
                x3Repository.count();



        Integer before =
                (int)customerRepository.count();



        List<X3CustomerDTO> customers =
                x3Repository.findCustomers();



        int inserted=0;


        int updated=0;




        for(X3CustomerDTO dto:customers){



            boolean exists =
                    customerRepository.existsById(
                            dto.getCustomerCode());



            XRCustomer customer =
                    customerRepository
                            .findById(dto.getCustomerCode())

                            .orElse(new XRCustomer());




            customer.setCustomerCode(
                    dto.getCustomerCode());



            customer.setCustomerName(
                    dto.getCustomerName());



            customer.setShortName(
                    dto.getShortName());



            customer.setCountryCode(
                    dto.getCountryCode());



            customer.setCurrencyCode(
                    dto.getCurrencyCode());



            customer.setActive(
                    dto.getActive());



            customer.setSyncedAt(
                    LocalDateTime.now());



            customerRepository.save(customer);



            if(exists)
                updated++;
            else
                inserted++;

        }



        Integer after =
                (int)customerRepository.count();




        return new SyncResult(

                x3Count,

                before,

                after,

                inserted,

                updated,

                0

        );



    }

}