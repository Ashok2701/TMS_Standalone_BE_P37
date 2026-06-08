package com.transport.tms.UserManagement.Impl;

import com.transport.tms.UserManagement.Dto.LoginRequestDTO;
import com.transport.tms.UserManagement.Dto.LoginResponseDTO;
import com.transport.tms.UserManagement.Dto.PermissionDTO;
import com.transport.tms.UserManagement.Entity.XRRoleModule;
import com.transport.tms.UserManagement.Entity.XRUser;
import com.transport.tms.UserManagement.Entity.XRUserSite;
import com.transport.tms.UserManagement.Repository.RoleModuleRepository;
import com.transport.tms.UserManagement.Repository.XRUserRepository;
import com.transport.tms.UserManagement.Repository.UserSiteRepository;
import com.transport.tms.UserManagement.Service.TokenService;
import com.transport.tms.UserManagement.Service.XRAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class XRAuthServiceImpl implements XRAuthService {

    private final XRUserRepository userRepository;

    private final UserSiteRepository userSiteRepository;

    private final RoleModuleRepository roleModuleRepository;

    private final TokenService tokenService;

    private final PasswordEncoder passwordEncoder;

    @Value("${transport.path}")
    private String contextPath;

    @Override
    public LoginResponseDTO login(
            LoginRequestDTO dto,
            HttpServletResponse response) {

        // find user

        XRUser user =
                userRepository.findByUsername(
                                dto.getUsername())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        // validate password

        if (!passwordEncoder.matches(
                dto.getPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Password is wrong");
        }

        // validate active

        if (!Boolean.TRUE.equals(
                user.getActive())) {

            throw new RuntimeException(
                    "User is inactive");
        }

        // load permissions

        List<XRRoleModule> roleModules =
                roleModuleRepository.findByRoleRoleId(
                        user.getRole().getRoleId());

        List<PermissionDTO> permissions =
                roleModules.stream()
                        .map(this::mapPermission)
                        .toList();

        // load sites

        List<String> sites =
                userSiteRepository.findByUserUserId(
                                user.getUserId())
                        .stream()
                        .map(XRUserSite::getSiteCode)
                        .toList();

        // generate token

        List<String> roles =
                List.of(
                        user.getRole().getRoleCode());

        String token =
                tokenService.generateAccessToken(
                        roles,
                        user.getUsername());

        // add cookie

        response.addCookie(
                generateCookie(
                        "token",
                        token));

        // build response

        return LoginResponseDTO.builder()

                .accessToken(token)

                .username(
                        user.getUsername())

                .fullName(
                        user.getFullName())

                .role(
                        user.getRole().getRoleName())

                .userType(
                        user.getUserType()
                                .getUserTypeName())

                .sites(sites)

                .permissions(permissions)

                .build();
    }

    @Override
    public void logout(
            HttpServletResponse response) {

        Cookie cookie =
                new Cookie("token", null);

        cookie.setHttpOnly(true);

        cookie.setMaxAge(0);

        cookie.setPath(contextPath);

        response.addCookie(cookie);
    }

    private Cookie generateCookie(
            String key,
            String value) {

        Cookie cookie =
                new Cookie(key, value);

        cookie.setHttpOnly(true);

        cookie.setMaxAge(
                60 * 60 * 24 * 365);

        cookie.setPath(contextPath);

        return cookie;
    }

    private PermissionDTO mapPermission(
            XRRoleModule rm) {

        PermissionDTO dto =
                new PermissionDTO();

        dto.setModuleCode(
                rm.getModule()
                        .getModuleCode());

        dto.setModuleName(
                rm.getModule()
                        .getModuleName());

        dto.setMenuPath(
                rm.getModule()
                        .getMenuPath());

        dto.setCanView(
                rm.getCanView());

        dto.setCanCreate(
                rm.getCanCreate());

        dto.setCanEdit(
                rm.getCanEdit());

        dto.setCanDelete(
                rm.getCanDelete());

        return dto;
    }
}