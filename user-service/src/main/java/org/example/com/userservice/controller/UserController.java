package org.example.com.userservice.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.example.com.userservice.pojo.User;
import org.example.com.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id, @RequestHeader(name = "color", required = false) String color) {
        User user = userService.getUser(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.getUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @DeleteMapping
    public ResponseEntity<Integer> deleteUser(@RequestBody User user) {
        Integer impactedRowNumber = userService.deleteUser(user.getId());
        return new ResponseEntity<>(impactedRowNumber, HttpStatus.OK);
    }

    @GetMapping("/csrf")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @GetMapping("/session")
    public String getSession(HttpServletRequest request) {
        return request.getSession().getId();
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return userService.verify(user);
    }


}
