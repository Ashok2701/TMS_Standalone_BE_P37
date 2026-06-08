package com.transport.tms.UserManagement.Service;

import com.transport.tms.UserManagement.Dto.RoleDTO;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    RoleDTO create(RoleDTO dto);

    List<RoleDTO> getAll();

    RoleDTO getById(UUID id);

    RoleDTO update(UUID id, RoleDTO dto);

    void delete(UUID id);
}