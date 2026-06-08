package com.transport.tms.Sync.Site.Repository;


import com.transport.tms.Sync.Site.Entity.XRSite;

import org.springframework.data.jpa.repository.JpaRepository;



public interface SiteRepository

        extends JpaRepository<XRSite,String>{




}