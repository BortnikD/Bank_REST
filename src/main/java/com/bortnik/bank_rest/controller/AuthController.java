package com.bortnik.bank_rest.controller;

import com.bortnik.bank_rest.controller.validator.UserValidator;
import com.bortnik.bank_rest.dto.ApiResponse;
import com.bortnik.bank_rest.dto.auth.AuthResponse;
import com.bortnik.bank_rest.dto.auth.UserLogin;
import com.bortnik.bank_rest.dto.auth.UserRegister;
import com.bortnik.bank_rest.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Authenticate user",
            description = "Authenticates a user using username and password, returning a JWT token."
    )
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody final UserLogin userLogin) {
        UserValidator.validateUsername(userLogin.getUsername());
        UserValidator.validatePassword(userLogin.getPassword());
        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .responseData(authenticationService.login(userLogin))
                .build();
    }

    @Operation(
            summary = "Register new user",
            description = "Creates a new user account and returns a JWT token."
    )
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody final UserRegister userRegister) {
        UserValidator.validateUsername(userRegister.getUsername());
        UserValidator.validatePassword(userRegister.getPassword());
        return ApiResponse.<AuthResponse>builder()
                .responseData(authenticationService.register(userRegister))
                .build();
    }
}
