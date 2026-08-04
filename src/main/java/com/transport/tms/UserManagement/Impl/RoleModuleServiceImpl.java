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

        // BUG FIX: deleteByRoleRoleId (a derived delete method, no
        // @Query) is implemented as "load matching entities, then queue
        // each for removal" — it goes through Hibernate's persistence
        // context, not an immediate bulk DELETE. Without flushing here,
        // Hibernate's default flush ordering can execute the saveAll()
        // INSERTs below BEFORE these queued deletes actually hit the
        // database, which is exactly what produced "duplicate key value
        // violates unique constraint uk_role_module ... already exists"
        // on an insert for a (role_id, module_id) pair whose delete you
        // already called — the delete just hadn't landed yet.
        repository.flush();

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