package org.example.com.monolithic.impl;

import org.example.com.monolithic.dao.UserRepo;
import org.example.com.monolithic.exception.UserNotFoundException;
import org.example.com.monolithic.pojo.User;
import org.example.com.monolithic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    UserRepo userRepo;

    @Autowired
    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
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
}
