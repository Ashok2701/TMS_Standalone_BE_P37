package com.transport.tms.Configuration.Document.Repository;


import com.transport.tms.Configuration.Document.Entity.DocumentConfig;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;


public interface DocumentConfigRepository
        extends JpaRepository<DocumentConfig, UUID> {


}