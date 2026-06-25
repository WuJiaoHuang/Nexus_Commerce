package org.example.com.monolithic.dao;

import org.example.com.monolithic.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User, String> {
    List<User> getUsersById(String id);

    @Query(value = "select u from User u where u.id = :userId")
    User findByUserId(String userId);
}
