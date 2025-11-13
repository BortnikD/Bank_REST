package com.bortnik.bank_rest.controller;

import com.bortnik.bank_rest.controller.validator.UserValidator;
import com.bortnik.bank_rest.dto.auth.AuthResponse;
import com.bortnik.bank_rest.dto.auth.UserLogin;
import com.bortnik.bank_rest.dto.auth.UserRegister;
import com.bortnik.bank_rest.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody final UserLogin userLogin) {
        UserValidator.validateUsername(userLogin.getUsername());
        UserValidator.validatePassword(userLogin.getPassword());
        return authenticationService.login(userLogin);
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody final UserRegister userRegister) {
        UserValidator.validateUsername(userRegister.getUsername());
        UserValidator.validatePassword(userRegister.getPassword());
        return authenticationService.register(userRegister);
    }
}
