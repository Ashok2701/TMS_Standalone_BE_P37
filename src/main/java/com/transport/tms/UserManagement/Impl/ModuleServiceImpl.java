package com.transport.tms.UserManagement.Impl;

import com.transport.tms.UserManagement.Dto.ModuleDTO;
import com.transport.tms.UserManagement.Entity.XRModule;
import com.transport.tms.UserManagement.Repository.ModuleRepository;
import com.transport.tms.UserManagement.Repository.RoleModuleRepository;
import com.transport.tms.UserManagement.Service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl
        implements ModuleService {

    private final ModuleRepository repository;

    private final RoleModuleRepository roleModuleRepository;

    @Override
    public ModuleDTO create(
            ModuleDTO dto) {

        // duplicate check

        if(repository.existsByModuleCode(
                dto.getModuleCode())) {

            throw new RuntimeException(
                    "Module already exists");
        }

        // build entity

        XRModule entity =
                new XRModule();

        entity.setModuleCode(
                dto.getModuleCode());

        entity.setModuleName(
                dto.getModuleName());

        entity.setMenuName(
                dto.getMenuName());

        entity.setMenuPath(
                dto.getMenuPath());

        entity.setIcon(
                dto.getIcon());

        entity.setDisplayOrder(
                dto.getDisplayOrder());

        entity.setActive(true);

        // save

        XRModule saved =
                repository.save(entity);

        return mapToDTO(saved);
    }

    @Override
    public List<ModuleDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public ModuleDTO getById(
            UUID id) {

        XRModule entity =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Module not found"));

        return mapToDTO(entity);
    }

    @Override
    public ModuleDTO update(
            UUID id,
            ModuleDTO dto) {

        XRModule entity =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Module not found"));

        entity.setModuleCode(
                dto.getModuleCode());

        entity.setModuleName(
                dto.getModuleName());

        entity.setMenuName(
                dto.getMenuName());

        entity.setMenuPath(
                dto.getMenuPath());

        entity.setIcon(
                dto.getIcon());

        entity.setDisplayOrder(
                dto.getDisplayOrder());

        entity.setActive(
                dto.getActive());

        XRModule updated =
                repository.save(entity);

        return mapToDTO(updated);
    }

    @Override
    public void delete(
            UUID id) {

        roleModuleRepository.deleteByModuleModuleId(id);

        repository.deleteById(id);
    }

    // mapper

    private ModuleDTO mapToDTO(
            XRModule entity) {

        ModuleDTO dto =
                new ModuleDTO();

        dto.setModuleId(
                entity.getModuleId());

        dto.setModuleCode(
                entity.getModuleCode());

        dto.setModuleName(
                entity.getModuleName());

        dto.setMenuName(
                entity.getMenuName());

        dto.setMenuPath(
                entity.getMenuPath());

        dto.setIcon(
                entity.getIcon());

        dto.setDisplayOrder(
                entity.getDisplayOrder());

        dto.setActive(
                entity.getActive());

        return dto;
    }
}