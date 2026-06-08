package com.transport.tms.UserManagement.Repository;

import com.transport.tms.UserManagement.Entity.XRRoleModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoleModuleRepository
        extends JpaRepository<XRRoleModule, UUID> {

    List<XRRoleModule> findByRoleRoleId(
            UUID roleId);

    void deleteByModuleModuleId(UUID moduleId);

    void deleteByRoleRoleId(
            UUID roleId);


        @Query("""
        SELECT rm
        FROM XRRoleModule rm
        JOIN FETCH rm.module
        JOIN FETCH rm.role
        WHERE rm.role.roleId = :roleId
    """)
        List<XRRoleModule> findByRoleIdWithModule(
                @Param("roleId") UUID roleId);


    }
