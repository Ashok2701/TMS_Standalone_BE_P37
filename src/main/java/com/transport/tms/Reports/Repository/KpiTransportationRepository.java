package com.transport.tms.Reports.Repository;

import com.transport.tms.Reports.Entity.KpiTransportationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface KpiTransportationRepository extends JpaRepository<KpiTransportationEntity, Long> {

    // TODO: replace with the real query once filter fields are finalized —
    // either a derived query method or a JPA Specification for dynamic filtering.
    List<KpiTransportationEntity> findByReportPeriodStartGreaterThanEqualAndReportPeriodEndLessThanEqual(
            LocalDate start, LocalDate end);
}
