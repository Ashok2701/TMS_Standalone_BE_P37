package com.transport.tms.UserManagement.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.transport.tms.Config.Anonymous;
import com.transport.tms.UserManagement.Dto.AccessTokenVO;
import com.transport.tms.UserManagement.Dto.UserDTO;
import com.transport.tms.UserManagement.Dto.UserVO;
import com.transport.tms.UserManagement.Entity.User;
import com.transport.tms.UserManagement.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping ("/login")
    @Anonymous
    public @ResponseBody ResponseEntity<Object> login(@RequestBody UserVO userVO, HttpServletResponse response) {
        try {
            UserVO result = userService.login(userVO, response);
            return ResponseEntity.ok(result);
        } catch (RuntimeException ex) {
            log.error("Login failed for username={}", userVO != null ? userVO.getXlogin() : null, ex);
            Map<String, String> error= new HashMap<>();
            error.put("message",ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping ("/logout")
    @Anonymous
    public @ResponseBody ResponseEntity<Object> logout(HttpServletResponse response) {
        userService.logOut(response);
        return ResponseEntity.status(HttpStatus.OK).body("sucess");
    }

    @PostMapping ("/create")
    public @ResponseBody ResponseEntity<Object> createUsers(AccessTokenVO accessTokenVO, @RequestBody User user) throws JsonProcessingException {
        try {
            User createdUser = userService.createUserWithAlignedSites(user);
            Map<String, String> map = new HashMap<>();
            map.put("success", "success");
            return ResponseEntity.ok(map);
        } catch (RuntimeException ex) {
            Map<String, String> error= new HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/getusers")
    public ResponseEntity<List<UserDTO>> getUserList(AccessTokenVO accessTokenVO) {
        List<UserDTO> users = userService.listUsers();
        return ResponseEntity.ok(users); // Return DTOs instead of User entities
    }

    @GetMapping("/{xlogin}")
    public ResponseEntity<User> getUser(@PathVariable String xlogin) {
        User user = userService.getUserWithAlignedSites(xlogin);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update/{xlogin}")
    public ResponseEntity<User> updateUser(@PathVariable String xlogin, @RequestBody User userDetails) {
        User updatedUser = userService.updateUserWithAlignedSites(xlogin, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/delete/{xlogin}")
    public ResponseEntity<String> deleteUser(@PathVariable String xlogin) {
        userService.deleteUserWithAlignedSites(xlogin);
        return ResponseEntity.ok("User and aligned sites deleted successfully");
    }

    @GetMapping("/list")
    public List<User> listUsers() {
        return userService.listfullUsers();
    }

}
