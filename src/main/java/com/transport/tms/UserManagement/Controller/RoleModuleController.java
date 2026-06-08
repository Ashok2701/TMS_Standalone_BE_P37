package com.transport.tms.UserManagement.Controller;

import com.transport.tms.UserManagement.Dto.RoleModuleDTO;
import com.transport.tms.UserManagement.Service.RoleModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/role-modules")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RoleModuleController {

    private final RoleModuleService service;

    // SAVE ROLE MODULES

    @PostMapping("/{roleId}")
    public void saveRoleModules(

            @PathVariable UUID roleId,

            @RequestBody
            List<RoleModuleDTO> dtos) {

        service.saveRoleModules(
                roleId,
                dtos);
    }

    // GET ROLE MODULES

    @GetMapping("/{roleId}")
    public List<RoleModuleDTO> getRoleModules(

            @PathVariable UUID roleId) {

        return service.getRoleModules(
                roleId);
    }
}