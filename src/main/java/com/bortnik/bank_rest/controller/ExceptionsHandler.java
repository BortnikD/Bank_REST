package com.bortnik.bank_rest.controller;

import com.bortnik.bank_rest.dto.ApiError;
import com.bortnik.bank_rest.exception.card.CardBlocked;
import com.bortnik.bank_rest.exception.card.CardNotFound;
import com.bortnik.bank_rest.exception.card.InsufficientFunds;
import com.bortnik.bank_rest.exception.security.AccessError;
import com.bortnik.bank_rest.exception.user.UserAlreadyExists;
import com.bortnik.bank_rest.exception.user.UserNotFound;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler(AccessError.class)
    ResponseEntity<ApiError> handleAccessError(AccessError accessError) {
        return buildResponseEntity(
                "Access Error",
                accessError.getMessage(),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(CardNotFound.class)
    ResponseEntity<ApiError> handleCardNotFound(CardNotFound cardNotFound) {
        return buildResponseEntity(
                "Card Not Found",
                cardNotFound.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(InsufficientFunds.class)
    ResponseEntity<ApiError> handleInsufficientFunds(InsufficientFunds insufficientFunds) {
        return buildResponseEntity(
                "Insufficient Funds",
                insufficientFunds.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(UserAlreadyExists.class)
    ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExists userAlreadyExists) {
        return buildResponseEntity(
                "User Already Exists",
                userAlreadyExists.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(UserNotFound.class)
    ResponseEntity<ApiError> handleUserNotFound(UserNotFound userNotFound) {
        return buildResponseEntity(
                "User Not Found",
                userNotFound.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ApiError> handleBadRequestException(BadRequestException badRequestException) {
        return buildResponseEntity(
                "Bad Request",
                badRequestException.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(CardBlocked.class)
    ResponseEntity<ApiError> handleCardBlocked(CardBlocked cardBlocked) {
        return buildResponseEntity(
                "Card Blocked",
                cardBlocked.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleGenericException(Exception exception) {
        return buildResponseEntity(
                "Internal Server Error",
                exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ApiError> buildResponseEntity(
            final String error,
            final String message,
            final HttpStatus status
    ) {
        final ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .error(error)
                .message(message)
                .status(status)
                .build();
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }
}
