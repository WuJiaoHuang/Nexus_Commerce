package org.example.com.userservice.impl;

import org.example.com.userservice.config.JwtService;
import org.example.com.userservice.dao.UserRepo;
import org.example.com.userservice.excpetionHandler.UserNotFoundException;
import org.example.com.userservice.pojo.User;
import org.example.com.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
            JwtService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;


    UserRepo userRepo;

    @Autowired
    public UserServiceImpl(UserRepo userRepo, AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public User getUser(String id) {
       return userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public List<User> getUsers() {
        return userRepo.findAll();
    }

    @Override
    public User saveUser(User user) {
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    @Override
    public Integer deleteUser(String id) {
        if (!userRepo.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepo.deleteById(id);
        return 1;
    }

    @Override
    public String verify(User user) {
       Authentication authentication = authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
       );
       if (authentication.isAuthenticated()) {
           return jwtService.generateToken(user.getUsername());
       }
       return "fail";

    }
}
