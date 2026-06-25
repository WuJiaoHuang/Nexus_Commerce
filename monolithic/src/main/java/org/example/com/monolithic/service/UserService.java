package org.example.com.monolithic.service;

import org.example.com.monolithic.pojo.User;

import java.util.List;

public interface UserService {
    User getUser(String id);
    List<User> getUsers();
    User saveUser(User user);
    Integer deleteUser(String id);
}
