package com.example.cloud.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.concurrent.atomic.AtomicInteger;

@RestControllerAdvice
public class ExceptionHandlers extends ResponseEntityExceptionHandler {
    private final AtomicInteger counter = new AtomicInteger(1);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Error> authorizeExceptionHandler(AuthenticationException e) {
        return new ResponseEntity<>(new Error(e.getMessage(), counter.getAndIncrement()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<Error> tokenExceptionHandler(TokenException e) {
        return new ResponseEntity<>(new Error(e.getMessage(), counter.getAndIncrement()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InputDataException.class)
    public ResponseEntity<Error> inputDataExceptionHandler(InputDataException e) {
        return new ResponseEntity<>(new Error(e.getMessage(), counter.getAndIncrement()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileException.class)
    public ResponseEntity<Error> fileExceptionHandler(FileException e) {
        return new ResponseEntity<>(new Error(e.getMessage(), counter.getAndIncrement()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Error> nullPointerExceptionHandler(NullPointerException e) {
        return new ResponseEntity<>(new Error(e.getMessage(), counter.getAndIncrement()),
                HttpStatus.BAD_REQUEST);
    }
}