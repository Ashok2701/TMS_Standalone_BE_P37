package com.transport.tms.UserManagement.Service;


import com.transport.tms.UserManagement.Dto.UserTypeDTO;

import java.util.List;
import java.util.UUID;

public interface UserTypeService {

    UserTypeDTO create(UserTypeDTO dto);

    List<UserTypeDTO> getAll();

    UserTypeDTO getById(UUID id);

    UserTypeDTO update(UUID id, UserTypeDTO dto);

    void delete(UUID id);

}