package com.transport.tms.UserManagement.Repository;

import com.transport.tms.UserManagement.Entity.XRModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ModuleRepository
        extends JpaRepository<XRModule, UUID> {

    Optional<XRModule> findByModuleCode(
            String moduleCode);

    boolean existsByModuleCode(
            String moduleCode);
}