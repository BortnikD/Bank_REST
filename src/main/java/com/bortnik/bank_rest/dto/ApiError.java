package com.bortnik.bank_rest.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
@Getter
public class ApiError {
    private LocalDateTime timestamp;
    private String error;
    private String message;
    private HttpStatus status;
}
