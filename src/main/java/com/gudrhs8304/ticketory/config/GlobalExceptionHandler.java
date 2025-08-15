package com.gudrhs8304.ticketory.config;

import com.gudrhs8304.ticketory.dto.error.ApiError;
import com.gudrhs8304.ticketory.exception.DuplicateLoginIdException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateLoginIdException.class)
    public ResponseEntity<ApiError> handleDuplicateLoginId(DuplicateLoginIdException ex) {
        ApiError body = new ApiError(
                "DUPLICATE_LOGIN_ID",
                ex.getMessage(),
                "/signup/duplicate" // 프론트가 이동할 경로
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body); // 409
    }
}