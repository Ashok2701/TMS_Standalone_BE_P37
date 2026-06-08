package com.transport.tms.transport.sqlserver.repository;

import com.transport.tms.transport.sqlserver.Entity.sites;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteX3Repository extends CrudRepository<sites, String> {

        public List<sites> findAll();

    }



