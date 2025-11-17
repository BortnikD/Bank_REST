package com.bortnik.bank_rest.controller;

import com.bortnik.bank_rest.dto.ApiError;
import com.bortnik.bank_rest.dto.ApiResponse;
import com.bortnik.bank_rest.exception.BadCredentials;
import com.bortnik.bank_rest.exception.BadRequest;
import com.bortnik.bank_rest.exception.card.*;
import com.bortnik.bank_rest.exception.security.AccessError;
import com.bortnik.bank_rest.exception.user.UserAlreadyExists;
import com.bortnik.bank_rest.exception.user.UserNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@ControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler(AccessError.class)
    ResponseEntity<ApiResponse<ApiError>> handleAccessError(AccessError accessError) {
        return buildResponseEntity(
                "Access Error",
                accessError.getMessage(),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(CardNotFound.class)
    ResponseEntity<ApiResponse<ApiError>> handleCardNotFound(CardNotFound cardNotFound) {
        return buildResponseEntity(
                "Card Not Found",
                cardNotFound.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(InsufficientFunds.class)
    ResponseEntity<ApiResponse<ApiError>> handleInsufficientFunds(InsufficientFunds insufficientFunds) {
        return buildResponseEntity(
                "Insufficient Funds",
                insufficientFunds.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(UserAlreadyExists.class)
    ResponseEntity<ApiResponse<ApiError>> handleUserAlreadyExists(UserAlreadyExists userAlreadyExists) {
        return buildResponseEntity(
                "User Already Exists",
                userAlreadyExists.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(UserNotFound.class)
    ResponseEntity<ApiResponse<ApiError>> handleUserNotFound(UserNotFound userNotFound) {
        return buildResponseEntity(
                "User Not Found",
                userNotFound.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BadRequest.class)
    ResponseEntity<ApiResponse<ApiError>> handleBadRequest(BadRequest badRequestException) {
        return buildResponseEntity(
                "Bad Request",
                badRequestException.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(CardBlocked.class)
    ResponseEntity<ApiResponse<ApiError>> handleCardBlocked(CardBlocked cardBlocked) {
        return buildResponseEntity(
                "Card Blocked",
                cardBlocked.getMessage(),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(CardExpired.class)
    ResponseEntity<ApiResponse<ApiError>> handleCardExpired(CardExpired cardExpired) {
        return buildResponseEntity(
                "Card Expired",
                cardExpired.getMessage(),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(CardsAreTheSame.class)
    ResponseEntity<ApiResponse<ApiError>> handleCardsAreTheSame(CardsAreTheSame cardsAreTheSame) {
        return buildResponseEntity(
                "Card Are The Same",
                cardsAreTheSame.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IncorrectAmount.class)
    ResponseEntity<ApiResponse<ApiError>> handleIncorrectAmount(IncorrectAmount incorrectAmount) {
        return buildResponseEntity(
                "Card Are The Same",
                incorrectAmount.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(CardAlreadyActivated.class)
    ResponseEntity<ApiResponse<ApiError>> handleCardAlreadyActivated(CardAlreadyActivated cardAlreadyActivated) {
        return buildResponseEntity(
                "Card Already Activated",
                cardAlreadyActivated.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(CardAlreadyBlocked.class)
    ResponseEntity<ApiResponse<ApiError>> handleCardAlreadyActivated(CardAlreadyBlocked cardAlreadyBlocked) {
        return buildResponseEntity(
                "Card Already Blocked",
                cardAlreadyBlocked.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse<ApiError>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        return buildResponseEntity(
                "Bad Request",
                "Invalid parameter: " + exception.getName(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiResponse<ApiError>> handleNoResourceFoundException(NoResourceFoundException exception) {
        return buildResponseEntity(
                "Resource Not Found",
                exception.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BadCredentials.class)
    ResponseEntity<ApiResponse<ApiError>> handleBadCredentials(BadCredentials badCredentials) {
        return buildResponseEntity(
                "Bad Credentials",
                badCredentials.getMessage(),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiResponse<ApiError>> handleBadCredentialsException(BadCredentialsException exception) {
        return buildResponseEntity(
                "Bad Credentials",
                exception.getMessage(),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<ApiError>> handleGenericException(Exception exception) {
        return buildResponseEntity(
                "Internal Server Error",
                exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ApiResponse<ApiError>> buildResponseEntity(
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

        return ResponseEntity.status(apiError.getStatus()).body(
                ApiResponse.<ApiError>builder()
                        .success(false)
                        .apiError(apiError)
                        .build()
        );
    }
}
