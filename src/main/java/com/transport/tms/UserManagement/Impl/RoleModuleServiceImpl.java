package com.transport.tms.UserManagement.Impl;

import com.transport.tms.UserManagement.Dto.RoleModuleDTO;
import com.transport.tms.UserManagement.Entity.XRModule;
import com.transport.tms.UserManagement.Entity.XRRole;
import com.transport.tms.UserManagement.Entity.XRRoleModule;
import com.transport.tms.UserManagement.Repository.ModuleRepository;
import com.transport.tms.UserManagement.Repository.RoleModuleRepository;
import com.transport.tms.UserManagement.Repository.XRRoleRepository;
import com.transport.tms.UserManagement.Service.RoleModuleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleModuleServiceImpl
        implements RoleModuleService {

    private final RoleModuleRepository repository;

    private final XRRoleRepository roleRepository;

    private final ModuleRepository moduleRepository;

    @Transactional
    @Override
    public void saveRoleModules(
            UUID roleId,
            List<RoleModuleDTO> dtos) {

        // fetch role

        XRRole role =
                roleRepository.findById(roleId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Role not found"));

        // remove old permissions

        repository.deleteByRoleRoleId(roleId);

        List<XRRoleModule> entities =
                new ArrayList<>();

        for(RoleModuleDTO dto : dtos) {

            XRModule module =
                    moduleRepository.findById(
                                    dto.getModuleId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Module not found"));

            XRRoleModule entity =
                    new XRRoleModule();

            entity.setRole(role);

            entity.setModule(module);

            entity.setCanView(
                    dto.getCanView());

            entity.setCanCreate(
                    dto.getCanCreate());

            entity.setCanEdit(
                    dto.getCanEdit());

            entity.setCanDelete(
                    dto.getCanDelete());

            entities.add(entity);
        }

        repository.saveAll(entities);
    }

    @Override
    public List<RoleModuleDTO> getRoleModules(
            UUID roleId) {

        return repository.findByRoleIdWithModule(
                        roleId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // mapper

    private RoleModuleDTO mapToDTO(
            XRRoleModule entity) {

        RoleModuleDTO dto =
                new RoleModuleDTO();

        dto.setModuleId(
                entity.getModule()
                        .getModuleId());

        dto.setModuleName(
                entity.getModule()
                        .getModuleName());

        dto.setCanView(
                entity.getCanView());

        dto.setCanCreate(
                entity.getCanCreate());

        dto.setCanEdit(
                entity.getCanEdit());

        dto.setCanDelete(
                entity.getCanDelete());

        return dto;
    }
}