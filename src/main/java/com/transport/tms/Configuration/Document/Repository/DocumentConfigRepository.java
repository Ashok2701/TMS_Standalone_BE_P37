package com.transport.tms.Configuration.Document.Repository;

import com.transport.tms.Configuration.Document.Entity.DocumentConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentConfigRepository
        extends JpaRepository<DocumentConfig, UUID> {

    // All active configs
    List<DocumentConfig> findByActiveTrue();

    // Filter by document type (e.g. "SON", "INV", "PO")
    List<DocumentConfig> findByDocumentType(String documentType);

    // All distinct document types (for dropdown filter)
    List<DocumentConfig> findByActiveTrueOrderByDocumentTypeAsc();
}
