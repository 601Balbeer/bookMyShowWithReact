package com.bms.bms_core.exceptions;


import com.bms.bms_core.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Handles response status exception and returns the corresponding response. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(status.getReasonPhrase())
                .error(status.toString())
                .message(e.getReason())
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }


    /** Handles internal server error and returns the corresponding response. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternalServerError(
            Exception e
    ){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /** Handles user not found and returns the corresponding response. */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException e
    ){
     ErrorResponse errorResponse = ErrorResponse.builder()
             .timeStamp(LocalDateTime.now())
             .status(HttpStatus.BAD_REQUEST.toString())
             .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
             .message(e.getMessage())
             .build();
     return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /** Handles record not found exception and returns the corresponding response. */
    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRecordNotFoundException(
            RecordNotFoundException e
    ){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.toString())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /** Returns a 400 error response when show times overlap. */
    @ExceptionHandler(OverlappingTimeException.class)
    public ResponseEntity<ErrorResponse> handleOverlappingTimeException(
            OverlappingTimeException e
    ){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.toString())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /** Returns a 401 error response for invalid credentials. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleOverlappingTimeException(
            InvalidCredentialsException e
    ){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.toString())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

}
