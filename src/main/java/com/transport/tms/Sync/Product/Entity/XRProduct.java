package com.transport.tms.Sync.Product.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "xr_product", schema = "tms")
@Getter
@Setter
public class XRProduct {

    @Id
    @Column(name = "product_code")
    private String productCode;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "product_category")
    private String productCategory;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(name = "sales_unit")
    private String salesUnit;

    @Column(name = "net_weight")
    private Double netWeight;

    @Column(name = "gross_weight")
    private Double grossWeight;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "weight_unit")
    private String weightUnit;

    @Column(name = "volume_unit")
    private String volumeUnit;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
}
