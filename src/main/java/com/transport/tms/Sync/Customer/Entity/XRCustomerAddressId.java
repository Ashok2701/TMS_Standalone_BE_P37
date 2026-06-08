package com.transport.tms.Sync.Customer.Entity;

import java.io.Serializable;
import java.util.Objects;

public class XRCustomerAddressId implements Serializable {

    private String customerCode;
    private String addressCode;

    public XRCustomerAddressId() {}

    public XRCustomerAddressId(String customerCode, String addressCode) {
        this.customerCode = customerCode;
        this.addressCode = addressCode;
    }

    public String getCustomerCode() { return customerCode; }
    public String getAddressCode()  { return addressCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XRCustomerAddressId)) return false;
        XRCustomerAddressId that = (XRCustomerAddressId) o;
        return Objects.equals(customerCode, that.customerCode)
            && Objects.equals(addressCode, that.addressCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerCode, addressCode);
    }
}
