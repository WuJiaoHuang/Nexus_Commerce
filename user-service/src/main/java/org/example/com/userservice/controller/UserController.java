package org.example.com.userservice.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.example.com.common.result.Result;
import org.example.com.userservice.pojo.User;
import org.example.com.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable String id, @RequestHeader(name = "color", required = false) String color) {
        User user = userService.getUser(id);
        return Result.success(user);
    }

    @GetMapping
    public Result<List<User>> getUsers() {
        List<User> users = userService.getUsers();
        return Result.success(users);
    }

    @PostMapping
    public ResponseEntity<Result<User>> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(HttpStatus.CREATED.value(), "created", savedUser));
    }

    @DeleteMapping
    public Result<Integer> deleteUser(@RequestBody User user) {
        Integer impactedRowNumber = userService.deleteUser(user.getId());
        return Result.success(impactedRowNumber);
    }

    @GetMapping("/csrf")
    public Result<CsrfToken> getCsrfToken(HttpServletRequest request) {
        return Result.success((CsrfToken) request.getAttribute("_csrf"));
    }

    @GetMapping("/session")
    public Result<String> getSession(HttpServletRequest request) {
        return Result.success(request.getSession().getId());
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        return Result.success(userService.verify(user));
    }


}
