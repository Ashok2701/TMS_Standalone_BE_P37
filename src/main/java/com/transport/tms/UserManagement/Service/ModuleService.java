package com.transport.tms.UserManagement.Service;

import com.transport.tms.UserManagement.Dto.ModuleDTO;

import java.util.List;
import java.util.UUID;

public interface ModuleService {

    ModuleDTO create(ModuleDTO dto);

    List<ModuleDTO> getAll();

    ModuleDTO getById(UUID id);

    ModuleDTO update(
            UUID id,
            ModuleDTO dto);

    void delete(UUID id);
}