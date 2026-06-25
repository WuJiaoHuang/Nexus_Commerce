package org.example.com.userservice.excpetionHandler;


import org.example.com.userservice.pojo.User;
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
