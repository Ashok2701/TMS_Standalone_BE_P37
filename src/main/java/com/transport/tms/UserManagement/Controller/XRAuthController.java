package com.transport.tms.UserManagement.Controller;

import com.transport.tms.Config.Anonymous;
import com.transport.tms.UserManagement.Dto.LoginRequestDTO;
import com.transport.tms.UserManagement.Dto.LoginResponseDTO;
import com.transport.tms.UserManagement.Service.XRAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class XRAuthController {

    private final XRAuthService authService;

    @PostMapping("/login")
    @Anonymous
    public ResponseEntity<Object> login(

            @RequestBody LoginRequestDTO dto,

            HttpServletResponse response) {

        try {

            LoginResponseDTO result =
                    authService.login(dto, response);

            return ResponseEntity.ok(result);

        } catch (RuntimeException ex) {

            log.error(
                    "Login failed for username={}",
                    dto.getUsername(),
                    ex);

            Map<String, String> error =
                    new HashMap<>();

            error.put("message",
                    ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletResponse response) {

        authService.logout(response);

        return ResponseEntity.ok("success");
    }
}