package com.bortnik.bank_rest.service;

import com.bortnik.bank_rest.dto.user.UserCreateDTO;
import com.bortnik.bank_rest.dto.user.UserDTO;
import com.bortnik.bank_rest.entity.Role;
import com.bortnik.bank_rest.entity.User;
import com.bortnik.bank_rest.exception.user.UserAlreadyExists;
import com.bortnik.bank_rest.exception.user.UserNotFound;
import com.bortnik.bank_rest.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserService userService = new UserService(userRepository);

    @Test
    public void createUser_success() {
        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                .username("user")
                .password("password")
                .role(Role.USER)
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .password("password")
                .role(Role.USER)
                .build();

        UserDTO savedUserDTO = UserDTO.builder()
                .id(savedUser.getId())
                .username("user")
                .role(Role.USER)
                .build();

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        assertEquals(savedUserDTO, userService.createUser(userCreateDTO));
    }

    @Test
    public void createUser_ShouldThrowUserAlreadyExists() {
        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                .username("existingUser")
                .password("password")
                .role(Role.USER)
                .build();

        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        var exception = assertThrows(UserAlreadyExists.class, () ->
            userService.createUser(userCreateDTO)
        );

        assertEquals("User with username existingUser already exists", exception.getMessage());
    }

    @Test
    public void makeAdmin_success() {
        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .username("user")
                .password("password")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        assertEquals(Role.ADMIN, userService.makeAdmin(userId).getRole());
    }

    @Test
    public void makeAdmin_ShouldThrowUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        var exception = assertThrows(UserNotFound.class, () ->
            userService.makeAdmin(userId)
        );

        assertEquals("User with id " + userId + " not found", exception.getMessage());
    }
}
