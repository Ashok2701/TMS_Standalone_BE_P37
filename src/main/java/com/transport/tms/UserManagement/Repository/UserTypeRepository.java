package com.transport.tms.UserManagement.Repository;

import com.transport.tms.UserManagement.Entity.XRUserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserTypeRepository
        extends JpaRepository<XRUserType, UUID> {

    Optional<XRUserType> findByUserTypeCode(
            String userTypeCode);

    boolean existsByUserTypeCode(
            String userTypeCode);
}