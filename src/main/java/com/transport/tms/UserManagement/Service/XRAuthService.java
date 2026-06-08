package com.transport.tms.UserManagement.Service;

import com.transport.tms.UserManagement.Dto.LoginRequestDTO;
import com.transport.tms.UserManagement.Dto.LoginResponseDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface XRAuthService {

    LoginResponseDTO login(
            LoginRequestDTO dto,
            HttpServletResponse response);

    void logout(
            HttpServletResponse response);
}
