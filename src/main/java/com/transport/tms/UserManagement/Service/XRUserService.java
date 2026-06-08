package com.transport.tms.UserManagement.Service;

import com.transport.tms.UserManagement.Dto.UserRequestDTO;
import com.transport.tms.UserManagement.Dto.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface XRUserService {

    UserResponseDTO create(
            UserRequestDTO dto);

    List<UserResponseDTO> getAll();

    UserResponseDTO getById(
            UUID id);

    UserResponseDTO update(
            UUID id,
            UserRequestDTO dto);

    void delete(
            UUID id);
}