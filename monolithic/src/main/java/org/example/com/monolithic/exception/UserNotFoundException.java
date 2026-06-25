package org.example.com.monolithic.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String id) {
        super("User not found: " + id);
    }

}
