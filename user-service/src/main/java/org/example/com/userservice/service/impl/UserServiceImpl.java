package org.example.com.userservice.service.impl;

import org.example.com.common.exception.BusinessException;
import org.example.com.common.exception.ErrorCode;
import org.example.com.userservice.config.JwtService;
import org.example.com.userservice.mapper.UserMapper;
import org.example.com.userservice.pojo.User;
import org.example.com.userservice.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getUser(String id) {
       User user = userMapper.selectById(id);
       if (user == null) {
           throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在: " + id);
       }
       return user;
    }

    @Override
    public List<User> getUsers() {
        return userMapper.selectList(null);
    }

    @Override
    public User saveUser(User user) {
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return user;
    }

    @Override
    public Integer deleteUser(String id) {
        if (userMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在: " + id);
        }
        return userMapper.deleteById(id);
    }

    @Override
    public String verify(User user) {
       Authentication authentication = authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
       );
       if (authentication.isAuthenticated()) {
           return jwtService.generateToken(user.getUsername());
       }
       throw new BusinessException(ErrorCode.UNAUTHORIZED);

    }
}
