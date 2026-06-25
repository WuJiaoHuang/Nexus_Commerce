package org.example.com.monolithic.controller;

import org.example.com.monolithic.pojo.User;
import org.example.com.monolithic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        User user=  userService.getUser(id);
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


}
