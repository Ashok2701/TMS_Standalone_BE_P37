package com.transport.tms.UserManagement.Service;

import com.transport.tms.UserManagement.Dto.RoleModuleDTO;

import java.util.List;
import java.util.UUID;

public interface RoleModuleService {

    void saveRoleModules(
            UUID roleId,
            List<RoleModuleDTO> dtos);

    List<RoleModuleDTO> getRoleModules(
            UUID roleId);
}