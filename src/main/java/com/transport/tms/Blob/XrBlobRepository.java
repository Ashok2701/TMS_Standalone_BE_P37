package com.transport.tms.Blob;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface XrBlobRepository extends JpaRepository<XrBlob, Long> {

    Optional<XrBlob> findByEntityTypeAndEntityCodeAndBlobType(
            String entityType, String entityCode, String blobType);

    void deleteByEntityTypeAndEntityCode(String entityType, String entityCode);
}
