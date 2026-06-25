package org.example.com.userservice.service;


import org.example.com.userservice.pojo.User;

import java.util.List;

public interface UserService {
    User getUser(String id);
    List<User> getUsers();
    User saveUser(User user);
    Integer deleteUser(String id);
    String verify(User user);
}
