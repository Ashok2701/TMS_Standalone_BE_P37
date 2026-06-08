package com.transport.tms.UserManagement.Service;

import com.transport.tms.UserManagement.Dto.*;
import com.transport.tms.UserManagement.Entity.*;
import com.transport.tms.UserManagement.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;
    @Value("${transport.path}")
    private String contextPath;
    @Value("${db.schema}")
    private String dbSchema;
    @Autowired
    private EntityManager entityManager;

    public UserVO login(@NotNull UserVO userVO, HttpServletResponse response) {
        User user = this.userRepository.findByXloginAndXpswd(userVO.getXlogin(), userVO.getXpswd());
        if(Objects.isNull(user)) {
            throw new RuntimeException("User not found or password is wrong");
        }
        if(!Boolean.TRUE.equals(user.getXact())) {
            throw new RuntimeException("User is not active");
        }
        userVO = new UserVO();
        userVO.setXusrname(user.getXusrname());
        userVO.setXlogin(user.getXlogin());
        userVO.setRouteplannerflg(nzb(user.getRouteplannerflg()));
        userVO.setSchedulerflg(nzb(user.getSchedulerflg()));
        userVO.setCalendarrpflg(nzb(user.getCalendarrpflg()));
        userVO.setMapviewrpflg(nzb(user.getMapviewrpflg()));
        userVO.setScreportsflg(nzb(user.getScreportsflg()));
        userVO.setFleetmgmtflg(nzb(user.getFleetmgmtflg()));
        userVO.setUsermgmtflg(nzb(user.getUsermgmtflg()));
        userVO.setRemovePicktcktflg(nzb(user.getRemovePicktcktflg()));
        userVO.setAddPicktcktflg(nzb(user.getAddPicktcktflg()));
        userVO.setXact(true);
        List<String> permissions = new ArrayList();
        permissions.add("Admin");
        userVO.setAccessToken(this.tokenService.generateAccessToken(permissions, user.getXusrname()));
        try {
            response.addCookie(this.generateCookie("token", userVO.getAccessToken()));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return userVO;
    }
    private Cookie generateCookie(final String key, final String value) {
        final Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24 * 365 * 10);
        cookie.setPath(contextPath);
        return cookie;
    }

    public void logOut(HttpServletResponse response) {
        final Cookie cookie = new Cookie("token", "value");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath(contextPath);
        try {
            response.addCookie(cookie);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User createUserWithAlignedSites(User user) {
        if (user == null || user.getXlogin() == null || user.getXlogin().isBlank()) {
            throw new RuntimeException("username is required for user creation");
        }
        if (user.getAuuid() == null) {
            user.setAuuid(UUID.randomUUID());
        }
        if (user.getCreusr() == null || user.getCreusr().isBlank()) {
            user.setCreusr(user.getXlogin());
        }
        if (user.getUpdusr() == null || user.getUpdusr().isBlank()) {
            user.setUpdusr(user.getCreusr());
        }
        user.setCredattim(new Date());
        user.setUpddattim(new Date());
        if (user.getAlignedSites() != null) {
            user.getAlignedSites().forEach(site -> site.setUser(user));
        }
        return userRepository.save(user);
    }

    private Boolean nzb(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public List<UserDTO> listUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setXlogin(user.getXlogin());
                    dto.setXusrname(user.getXusrname());
                    dto.setXact(user.getXact());
                    dto.setEmail(user.getEmail());
                    dto.setLngmain(user.getLngmain());
                    dto.setLansec(user.getLansec());
                    dto.setRole(1);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public User getUserWithAlignedSites(String xlogin) {
        return userRepository.findByXlogin(xlogin).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUserWithAlignedSites(String xlogin, User userDetails) {
        User user = userRepository.findByXlogin(xlogin).orElseThrow(() -> new RuntimeException("User not found"));
        user.setXusrname(userDetails.getXusrname());
        user.setEmail(userDetails.getEmail());
        user.setAddPicktcktflg(userDetails.getAddPicktcktflg());
        user.setLansec(userDetails.getLansec());
        user.setLngmain(userDetails.getLngmain());
        user.setBpadd(userDetails.getBpadd());
        user.setBpadd1(userDetails.getBpadd1());
        user.setBpadd2(userDetails.getBpadd2());
        user.setCalendarrpflg(userDetails.getCalendarrpflg());
        user.setCity(userDetails.getCity());
        user.setCountry(userDetails.getCountry());
        user.setFleetmgmtflg(userDetails.getFleetmgmtflg());
        user.setMapviewrpflg(userDetails.getMapviewrpflg());
        user.setPhone(userDetails.getPhone());
        user.setPincode(userDetails.getPincode());
        user.setRemovePicktcktflg(userDetails.getRemovePicktcktflg());
        user.setRouteplannerflg(userDetails.getRouteplannerflg());
        user.setSchedulerflg(userDetails.getSchedulerflg());
        user.setScreportsflg(userDetails.getScreportsflg());
        user.setState(userDetails.getState());
        user.setTel(userDetails.getTel());
        user.setUsermgmtflg(userDetails.getUsermgmtflg());
        user.setXpswd(userDetails.getXpswd());
        user.setXact(userDetails.getXact());
        user.setUpddattim(new Date());
        List<String> existingSites = new ArrayList<>();
        user.getAlignedSites().forEach(site->{
            existingSites.add(site.getFcy());
        });
        List<String> alignedSites = new ArrayList<>();
        userDetails.getAlignedSites().forEach(site->{
            alignedSites.add(site.getFcy());
        });
        if(!existingSites.isEmpty()){
            existingSites.forEach(fcy->{
                if(!alignedSites.contains(fcy)){
                    deleteSiteForUserUpdate(userDetails.getXlogin(), fcy);
                }
            });
        }
        user.getAlignedSites().clear();
        userDetails.getAlignedSites().forEach(site -> site.setUser(user));
        user.getAlignedSites().addAll(userDetails.getAlignedSites());
        return userRepository.save(user);
    }

    private void deleteSiteForUserUpdate(String user, String fcy) {
        entityManager.createNativeQuery(MessageFormat.format("delete from {0}.{1} where xaus= ''{2}'' and xfcy= ''{3}'' ", dbSchema, "xx10cuserd", user, fcy)).executeUpdate();
    }

    public void deleteUserWithAlignedSites(String xlogin) {
        User user = userRepository.findByXlogin(xlogin).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public List<User> listfullUsers() {
        return userRepository.findAll();
    }

}
