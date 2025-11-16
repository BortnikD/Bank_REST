package com.bortnik.bank_rest.controller;

import com.bortnik.bank_rest.controller.validator.UserValidator;
import com.bortnik.bank_rest.dto.ApiError;
import com.bortnik.bank_rest.dto.auth.AuthResponse;
import com.bortnik.bank_rest.dto.auth.UserLogin;
import com.bortnik.bank_rest.dto.auth.UserRegister;
import com.bortnik.bank_rest.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid username or password format",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Incorrect credentials",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/login")
    public AuthResponse login(@RequestBody final UserLogin userLogin) {
        UserValidator.validateUsername(userLogin.getUsername());
        UserValidator.validatePassword(userLogin.getPassword());
        return authenticationService.login(userLogin);
    }

    @Operation(
            summary = "Register new user",
            description = "Creates a new user account and returns a JWT token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid username or password format",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/register")
    public AuthResponse register(@RequestBody final UserRegister userRegister) {
        UserValidator.validateUsername(userRegister.getUsername());
        UserValidator.validatePassword(userRegister.getPassword());
        return authenticationService.register(userRegister);
    }
}
