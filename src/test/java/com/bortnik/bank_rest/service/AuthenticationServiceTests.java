package com.bortnik.bank_rest.service;

import com.bortnik.bank_rest.dto.auth.AuthResponse;
import com.bortnik.bank_rest.dto.auth.UserLogin;
import com.bortnik.bank_rest.dto.auth.UserRegister;
import com.bortnik.bank_rest.dto.user.UserCreateDTO;
import com.bortnik.bank_rest.dto.user.UserDTO;
import com.bortnik.bank_rest.entity.Role;
import com.bortnik.bank_rest.exception.BadCredentials;
import com.bortnik.bank_rest.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTests {

    private final UserService userService = mock(UserService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);

    private final AuthenticationService authenticationService = new AuthenticationService(
            userService,
            passwordEncoder,
            jwtTokenProvider,
            authenticationManager
    );

    @Test
    void register_success() {
        UserRegister userRegister = UserRegister.builder()
                .username("testuser")
                .password("password123")
                .build();

        String encodedPassword = "encodedPassword123";
        String generatedToken = "jwt.token.here";

        UserDTO createdUser = UserDTO.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .role(Role.USER)
                .build();

        when(passwordEncoder.encode(userRegister.getPassword())).thenReturn(encodedPassword);
        when(userService.createUser(any(UserCreateDTO.class))).thenReturn(createdUser);
        when(jwtTokenProvider.generateToken(createdUser.getUsername(), List.of(Role.USER.toString())))
                .thenReturn(generatedToken);

        AuthResponse response = authenticationService.register(userRegister);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(generatedToken, response.getToken());

        ArgumentCaptor<UserCreateDTO> userCaptor = ArgumentCaptor.forClass(UserCreateDTO.class);
        verify(userService).createUser(userCaptor.capture());

        UserCreateDTO captured = userCaptor.getValue();
        assertEquals("testuser", captured.getUsername());
        assertEquals(encodedPassword, captured.getPassword());
        assertEquals(Role.USER, captured.getRole());

        verify(passwordEncoder).encode(userRegister.getPassword());
        verify(jwtTokenProvider).generateToken(createdUser.getUsername(), List.of(Role.USER.toString()));
    }

    @Test
    void login_success() {
        UserLogin userLogin = UserLogin.builder()
                .username("testuser")
                .password("password123")
                .build();

        String generatedToken = "jwt.token.here";

        Authentication authentication = mock(Authentication.class);
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER"));

        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(eq(userLogin.getUsername()), anyCollection()))
                .thenReturn(generatedToken);

        AuthResponse response = authenticationService.login(userLogin);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(generatedToken, response.getToken());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());

        UsernamePasswordAuthenticationToken captured = authCaptor.getValue();
        assertEquals("testuser", captured.getPrincipal());
        assertEquals("password123", captured.getCredentials());

        verify(jwtTokenProvider).generateToken(eq(userLogin.getUsername()), anyCollection());
    }

    @Test
    void login_shouldThrowBadCredentials_whenAuthenticationFails() {
        UserLogin userLogin = UserLogin.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        var exception = assertThrows(BadCredentials.class, () ->
                authenticationService.login(userLogin));

        assertEquals("Invalid username or password", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyCollection());
    }
}