package com.transport.tms.UserManagement.ServiceImpl;

import com.transport.tms.UserManagement.Dto.UserRequestDTO;
import com.transport.tms.UserManagement.Dto.UserResponseDTO;
import com.transport.tms.UserManagement.Entity.*;
import com.transport.tms.UserManagement.Repository.*;
import com.transport.tms.UserManagement.Service.XRUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl
        implements XRUserService {

    private final XRUserRepository userRepository;

    private final UserSiteRepository userSiteRepository;

    private final XRRoleRepository roleRepository;

    private final UserTypeRepository userTypeRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserResponseDTO create(
            UserRequestDTO dto) {

        // duplicate username check

        if(userRepository.existsByUsername(
                dto.getUsername())) {

            throw new RuntimeException(
                    "Username already exists");
        }

        // fetch role

        XRRole role =
                roleRepository.findById(
                                dto.getRoleId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Role not found"));

        // fetch user type

        XRUserType userType =
                userTypeRepository.findById(
                                dto.getUserTypeId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User Type not found"));

        // build entity

        XRUser user = new XRUser();

        user.setUsername(dto.getUsername());

        user.setPassword(
                passwordEncoder.encode(
                        dto.getPassword()));

        user.setFullName(dto.getFullName());

        user.setEmail(dto.getEmail());

        user.setMobileNo(dto.getMobileNo());

        user.setRole(role);

        user.setUserType(userType);

        user.setActive(true);

        // save user

        XRUser savedUser =
                userRepository.save(user);

        // save sites if sent
        // (see update() for why this no longer gates on
        // userType.getRequiresSiteMapping() — always honor what the
        // client actually submitted)

        List<String> siteCodes = dto.getSites();
        if (siteCodes != null && !siteCodes.isEmpty()) {

            List<XRUserSite> sites =
                    siteCodes
                            .stream()
                            .map(site -> {

                                XRUserSite us =
                                        new XRUserSite();

                                us.setUser(savedUser);

                                us.setSiteCode(site);

                                us.setActive(true);

                                return us;

                            }).toList();

            userSiteRepository.saveAll(sites);
        }

        return buildResponse(savedUser);
    }

    @Override
    public List<UserResponseDTO> getAll() {

        return userRepository.findAllWithDetails()
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    public UserResponseDTO getById(
            UUID id) {

        XRUser user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        return buildResponse(user);
    }

    @Transactional
    @Override
    public UserResponseDTO update(
            UUID id,
            UserRequestDTO dto) {

        XRUser user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        XRRole role =
                roleRepository.findById(
                                dto.getRoleId())
                        .orElseThrow();

        XRUserType userType =
                userTypeRepository.findById(
                                dto.getUserTypeId())
                        .orElseThrow();

        user.setFullName(dto.getFullName());

        user.setEmail(dto.getEmail());

        user.setMobileNo(dto.getMobileNo());

        user.setRole(role);

        user.setUserType(userType);

        XRUser updated =
                userRepository.save(user);

        // remove old sites
        userSiteRepository.deleteByUserUserId(id);

        // BUG FIX: this used to be gated behind
        // userType.getRequiresSiteMapping() — but the delete above always
        // ran unconditionally, so if that flag wasn't set as expected for
        // this user's type, sites were wiped and never re-added, even
        // though the client explicitly sent a sites list. The client
        // already decides what to submit; the backend shouldn't silently
        // drop it. Always persist whatever was sent (null-safe).
        List<String> siteCodes = dto.getSites();
        if (siteCodes != null && !siteCodes.isEmpty()) {

            List<XRUserSite> sites =
                    siteCodes
                            .stream()
                            .map(site -> {

                                XRUserSite us =
                                        new XRUserSite();

                                us.setUser(updated);

                                us.setSiteCode(site);

                                us.setActive(true);

                                return us;

                            }).toList();

            userSiteRepository.saveAll(sites);
        }

        return buildResponse(updated);
    }

    @Transactional
    @Override
    public void delete(
            UUID id) {

        userSiteRepository.deleteByUserUserId(id);

        userRepository.deleteById(id);
    }

    // mapper

    private UserResponseDTO buildResponse(
            XRUser user) {

        UserResponseDTO dto =
                new UserResponseDTO();

        dto.setUserId(user.getUserId());

        dto.setUsername(user.getUsername());

        dto.setFullName(user.getFullName());

        dto.setEmail(user.getEmail());

        dto.setMobileNo(user.getMobileNo());

        dto.setRole(
                user.getRole().getRoleName());

        dto.setUserType(
                user.getUserType()
                        .getUserTypeName());

        dto.setActive(user.getActive());

        List<String> sites =
                userSiteRepository
                        .findByUserUserId(
                                user.getUserId())
                        .stream()
                        .map(XRUserSite::getSiteCode)
                        .toList();

        dto.setSites(sites);

        return dto;
    }
}