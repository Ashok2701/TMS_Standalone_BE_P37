package com.transport.tms.UserManagement.Controller;

import com.transport.tms.UserManagement.Dto.UserRequestDTO;
import com.transport.tms.UserManagement.Dto.UserResponseDTO;
import com.transport.tms.UserManagement.Service.XRUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class XRUserController {

    private final XRUserService service;

    // CREATE

    @PostMapping
    public UserResponseDTO create(
            @RequestBody UserRequestDTO dto) {

        return service.create(dto);
    }

    // GET ALL

    @GetMapping
    public List<UserResponseDTO> getAll() {

        return service.getAll();
    }

    // GET BY ID

    @GetMapping("/{id}")
    public UserResponseDTO getById(
            @PathVariable UUID id) {

        return service.getById(id);
    }

    // UPDATE

    @PutMapping("/{id}")
    public UserResponseDTO update(
            @PathVariable UUID id,

            @RequestBody UserRequestDTO dto) {

        return service.update(id, dto);
    }

    // DELETE

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id) {

        service.delete(id);
    }
}