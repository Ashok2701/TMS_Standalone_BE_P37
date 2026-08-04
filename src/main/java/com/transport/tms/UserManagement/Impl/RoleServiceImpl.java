package com.transport.tms.UserManagement.Impl;


import com.transport.tms.UserManagement.Dto.RoleDTO;
import com.transport.tms.UserManagement.Entity.XRRole;
import com.transport.tms.UserManagement.Repository.RoleModuleRepository;
import com.transport.tms.UserManagement.Repository.XRRoleRepository;
import com.transport.tms.UserManagement.Repository.XRUserRepository;
import com.transport.tms.UserManagement.Service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final XRRoleRepository repository;

    private final RoleModuleRepository roleModuleRepository;

    private final XRUserRepository userRepository;

    @Override
    public RoleDTO create(RoleDTO dto) {

        if(repository.existsByRoleCode(dto.getRoleCode())) {
            throw new RuntimeException("Role already exists");
        }

        XRRole role = new XRRole();

        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setActive(dto.getActive() != null ? dto.getActive() : true);

        XRRole saved = repository.save(role);

        return map(saved);
    }

    @Override
    public List<RoleDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public RoleDTO getById(UUID id) {

        XRRole role = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        return map(role);
    }

    @Override
    public RoleDTO update(UUID id, RoleDTO dto) {

        XRRole role = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setActive(dto.getActive());

        XRRole updated = repository.save(role);

        return map(updated);
    }

    @Override
    @Transactional
    // BUG FIX: this was a plain repository.deleteById(id) — the raw SQL
    // error you hit ('violates foreign key constraint fk_role ... still
    // referenced from table xr_role_modules') was Postgres correctly
    // blocking a delete that would have orphaned those rows, since
    // nothing here ever cleaned them up first.
    public void delete(UUID id) {

        repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        // xr_users.role_id has the same FK to xr_roles — deleting a role
        // still assigned to users would hit the identical constraint
        // violation there. Block with a clear message instead of letting
        // that raw SQL exception surface, same as the one you just saw.
        if (userRepository.existsByRoleRoleId(id)) {
            throw new RuntimeException(
                    "Cannot delete role — one or more users are still assigned to it. Reassign them to a different role first.");
        }

        // Safe to cascade — these are just this role's module
        // assignments (from Assign Modules to Roles), not user data.
        roleModuleRepository.deleteByRoleRoleId(id);

        repository.deleteById(id);
    }

    private RoleDTO map(XRRole role) {

        RoleDTO dto = new RoleDTO();

        dto.setRoleId(role.getRoleId());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleName(role.getRoleName());
        dto.setActive(role.getActive());

        return dto;
    }
}