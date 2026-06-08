package com.transport.tms.UserManagement.Impl;

import com.transport.tms.UserManagement.Dto.UserTypeDTO;
import com.transport.tms.UserManagement.Entity.XRUserType;
import com.transport.tms.UserManagement.Repository.UserTypeRepository;
import com.transport.tms.UserManagement.Service.UserTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserTypeServiceImpl
        implements UserTypeService {

    private final UserTypeRepository repository;

    @Override
    public UserTypeDTO create(
            UserTypeDTO dto) {

        // validation

        if(repository.existsByUserTypeCode(
                dto.getUserTypeCode())) {

            throw new RuntimeException(
                    "User Type already exists");
        }

        // entity mapping

        XRUserType entity =
                new XRUserType();

        entity.setUserTypeCode(
                dto.getUserTypeCode());

        entity.setUserTypeName(
                dto.getUserTypeName());

        entity.setRequiresSiteMapping(
                dto.getRequiresSiteMapping());

        entity.setActive(true);

        // save

        XRUserType saved =
                repository.save(entity);

        // return dto

        return mapToDTO(saved);
    }

    @Override
    public List<UserTypeDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public UserTypeDTO getById(
            UUID id) {

        XRUserType entity =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User Type not found"));

        return mapToDTO(entity);
    }

    @Override
    public UserTypeDTO update(
            UUID id,
            UserTypeDTO dto) {

        XRUserType entity =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User Type not found"));

        entity.setUserTypeCode(
                dto.getUserTypeCode());

        entity.setUserTypeName(
                dto.getUserTypeName());

        entity.setRequiresSiteMapping(
                dto.getRequiresSiteMapping());

        entity.setActive(
                dto.getActive());

        XRUserType updated =
                repository.save(entity);

        return mapToDTO(updated);
    }

    @Override
    public void delete(
            UUID id) {

        repository.deleteById(id);
    }

    // mapper method

    private UserTypeDTO mapToDTO(
            XRUserType entity) {

        UserTypeDTO dto =
                new UserTypeDTO();

        dto.setUserTypeId(
                entity.getUserTypeId());

        dto.setUserTypeCode(
                entity.getUserTypeCode());

        dto.setUserTypeName(
                entity.getUserTypeName());

        dto.setRequiresSiteMapping(
                entity.getRequiresSiteMapping());

        dto.setActive(
                entity.getActive());

        return dto;
    }
}