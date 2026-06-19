package com.transport.tms.RoutePlanner.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopEnrichmentRepository
        extends JpaRepository<StopEnrichment, StopEnrichment.StopEnrichmentId> {

    /**
     * Batch fetch enrichment rows for a list of customer codes + doc type.
     * One call per stop list (drops or pickups) after X3 fetch.
     * doc_type = 'DLV' for deliveries, 'PICK' for pick tickets.
     */
    @Query("""
        SELECT e FROM StopEnrichment e
        WHERE  e.customerCode IN :customerCodes
          AND  e.docType       = :docType
    """)
    List<StopEnrichment> findByCustomerCodesAndDocType(
            @Param("customerCodes") List<String> customerCodes,
            @Param("docType")       String docType);
}
