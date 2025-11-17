package com.bortnik.bank_rest.dto;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
@Value
public class ApiError {
    LocalDateTime timestamp;
    String error;
    String message;
    HttpStatus status;
}
