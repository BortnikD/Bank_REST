package com.bortnik.bank_rest.service;

import com.bortnik.bank_rest.dto.auth.AuthResponse;
import com.bortnik.bank_rest.dto.auth.UserLogin;
import com.bortnik.bank_rest.dto.auth.UserRegister;
import com.bortnik.bank_rest.dto.user.UserCreateDTO;
import com.bortnik.bank_rest.dto.user.UserDTO;
import com.bortnik.bank_rest.entity.Role;
import com.bortnik.bank_rest.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(UserRegister userRegister) {
        final Role userRole = Role.USER;
        final UserCreateDTO createUserDTO = UserCreateDTO.builder()
                .username(userRegister.getUsername())
                .password(passwordEncoder.encode(userRegister.getPassword()))
                .role(userRole)
                .build();
        final UserDTO user = userService.createUser(createUserDTO);
        final String token = jwtTokenProvider.generateToken(user.getUsername(), List.of(userRole.toString()));
        return AuthResponse.builder()
                .username(user.getUsername())
                .tokenType("Bearer")
                .token(token)
                .build();
    }

    public AuthResponse login(UserLogin userLogin) {
        final Authentication auth =  authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword())
        );

        final Collection<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        final String token = jwtTokenProvider.generateToken(userLogin.getUsername(), roles);
        return AuthResponse.builder()
                .username(userLogin.getUsername())
                .tokenType("Bearer")
                .token(token)
                .build();
    }
}
