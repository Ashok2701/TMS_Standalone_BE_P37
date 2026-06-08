package com.transport.tms.UserManagement.Repository;

import com.transport.tms.UserManagement.Entity.XRUserSite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSiteRepository
        extends JpaRepository<XRUserSite, UUID> {

    List<XRUserSite> findByUserUserId(
            UUID userId);

    void deleteByUserUserId(
            UUID userId);
}