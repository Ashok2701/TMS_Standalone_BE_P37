package com.transport.tms.UserManagement.Impl;


import com.transport.tms.UserManagement.Dto.RoleDTO;
import com.transport.tms.UserManagement.Entity.XRRole;
import com.transport.tms.UserManagement.Repository.XRRoleRepository;
import com.transport.tms.UserManagement.Service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final XRRoleRepository repository;

    @Override
    public RoleDTO create(RoleDTO dto) {

        if(repository.existsByRoleCode(dto.getRoleCode())) {
            throw new RuntimeException("Role already exists");
        }

        XRRole role = new XRRole();

        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setActive(true);

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
    public void delete(UUID id) {

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