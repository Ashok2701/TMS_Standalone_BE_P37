package com.transport.tms.Sync.Customer.Entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;



@Entity
@Table(name="xr_customer")
@Getter
@Setter
public class XRCustomer {


    @Id
    @Column(name="customer_code")
    private String customerCode;



    @Column(name="customer_name")
    private String customerName;



    @Column(name="short_name")
    private String shortName;



    @Column(name="country_code")
    private String countryCode;



    @Column(name="currency_code")
    private String currencyCode;



    @Column(name="active")
    private Boolean active;



    @Column(name="synced_at")
    private LocalDateTime syncedAt;


}