package org.example.com.userservice.excpetionHandler;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String id) {
        super("User not found: " + id);
    }

}
