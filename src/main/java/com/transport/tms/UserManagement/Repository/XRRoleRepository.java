package com.transport.tms.UserManagement.Repository;

import com.transport.tms.UserManagement.Entity.XRRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface XRRoleRepository
        extends JpaRepository<XRRole, UUID> {

    Optional<XRRole> findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);

}
