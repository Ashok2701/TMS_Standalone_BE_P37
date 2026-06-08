package com.transport.tms.UserManagement.Controller;

import com.transport.tms.UserManagement.Dto.UserTypeDTO;
import com.transport.tms.UserManagement.Service.UserTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-types")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserTypeController {

    private final UserTypeService service;

    // CREATE

    @PostMapping
    public UserTypeDTO create(
            @RequestBody UserTypeDTO dto) {

        return service.create(dto);
    }

    // GET ALL

    @GetMapping
    public List<UserTypeDTO> getAll() {

        return service.getAll();
    }

    // GET BY ID

    @GetMapping("/{id}")
    public UserTypeDTO getById(
            @PathVariable UUID id) {

        return service.getById(id);
    }

    // UPDATE

    @PutMapping("/{id}")
    public UserTypeDTO update(
            @PathVariable UUID id,

            @RequestBody UserTypeDTO dto) {

        return service.update(id, dto);
    }

    // DELETE

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id) {

        service.delete(id);
    }
}