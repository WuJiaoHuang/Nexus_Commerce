package org.example.com.monolithic.exceptionHandler;

import org.example.com.monolithic.exception.UserNotFoundException;
import org.example.com.monolithic.pojo.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<User> handleUserNotFoundException(UserNotFoundException e) {
        return new ResponseEntity<>(new User(), HttpStatus.NOT_FOUND);
    }
}
