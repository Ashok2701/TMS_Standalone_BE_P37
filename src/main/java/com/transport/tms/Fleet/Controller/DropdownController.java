package com.transport.tms.Fleet.Controller;

import com.transport.tms.Fleet.Entity.DropdownData;
import com.transport.tms.Fleet.Repository.CommonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dropdown data endpoints — serve data from SQL Server (X3) for use in
 * customer address configuration grids (vehicle categories, drivers).
 *
 * These read from X3 SQL Server tables directly via CommonRepository,
 * NOT from the empty Postgres xr_driver / xr_vehicle_category tables.
 */
@RestController
@RequestMapping("/api/dropdowns")
@RequiredArgsConstructor
public class DropdownController {

    private final CommonRepository commonRepository;

    // Vehicle categories (from XX10CCLASS in X3)
    // Used in: customer address → vehicle category grid
    @GetMapping("/vehicle-categories")
    public List<DropdownData> vehicleCategories() {
        return commonRepository.getVehicleClassList();
    }

    // Drivers (from XX10CDRIVER in X3)
    // Used in: customer address → driver grid
    @GetMapping("/drivers")
    public List<DropdownData> drivers() {
        return commonRepository.getDriverList();
    }

    // Sites (from FACILITY in X3)
    @GetMapping("/sites")
    public List<DropdownData> sites() {
        return commonRepository.getSiteList();
    }

    // Customers
    @GetMapping("/customers")
    public List<DropdownData> customers() {
        return commonRepository.getCustomerList();
    }

    // Carriers
    @GetMapping("/carriers")
    public List<DropdownData> carriers() {
        return commonRepository.getCarrierList();
    }

    // Countries
    @GetMapping("/countries")
    public List<DropdownData> countries() {
        return commonRepository.getCountryList();
    }

    // Trailer types
    @GetMapping("/trailer-types")
    public List<DropdownData> trailerTypes() {
        return commonRepository.getTrailerTypeList();
    }
}
