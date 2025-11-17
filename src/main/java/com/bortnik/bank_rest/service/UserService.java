package com.bortnik.bank_rest.service;

import com.bortnik.bank_rest.dto.user.UserCreateDTO;
import com.bortnik.bank_rest.dto.user.UserDTO;
import com.bortnik.bank_rest.entity.Role;
import com.bortnik.bank_rest.entity.User;
import com.bortnik.bank_rest.exception.user.UserAlreadyExists;
import com.bortnik.bank_rest.exception.user.UserNotFound;
import com.bortnik.bank_rest.repository.UserRepository;
import com.bortnik.bank_rest.util.mappers.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Создание нового пользователя.
     * @param userCreateDTO информация о создаваемом пользователе
     * @return {@code UserDTO} информация о созданном пользователе
     * @throws UserAlreadyExists если пользователь с таким именем уже существует
     */
    @Transactional
    public UserDTO createUser(final UserCreateDTO userCreateDTO) {
        log.info("Creating user with username: {}", userCreateDTO.getUsername());

        if (userRepository.existsByUsername(userCreateDTO.getUsername())) {
            log.warn("User with username {} already exists", userCreateDTO.getUsername());
            throw new UserAlreadyExists("User with username " + userCreateDTO.getUsername() + " already exists");
        }

        UserDTO user = UserMapper.toUserDTO(
                userRepository.save(
                        User.builder()
                                .username(userCreateDTO.getUsername())
                                .password(userCreateDTO.getPassword())
                                .role(userCreateDTO.getRole())
                                .build()
                )
        );

        log.info("User with username: {} created successfully", userCreateDTO.getUsername());

        return user;
    }

    public UserDTO getUserById(final UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("User with id " + userId + " not found"));
        return UserMapper.toUserDTO(user);
    }

    /**
     * Повышение пользователя до администратора. Функция для администратора.
     * @param userId идентификатор пользователя
     * @return {@code UserDTO} информация о пользователе с ролью администратора
     */
    @Transactional
    public UserDTO makeAdmin(final UUID userId) {
        log.info("Making user with id {} admin", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new UserNotFound("User with id " + userId + " not found");
                });

        user.setRole(Role.ADMIN);
        user.setUpdatedAt(LocalDateTime.now());
        UserDTO userDto = UserMapper.toUserDTO(userRepository.save(user));

        log.info("User with id {} is now an admin", userId);

        return userDto;
    }

    /**
     * Получение всех пользователей с пагинацией. Функция для администратора.
     * @param pageable параметры пагинации
     * @return страница с пользователями
     */
    public Page<UserDTO> getAllUsers(final Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toUserDTO);
    }

    /**
     * Получение всех пользователей по роли с пагинацией. Функция для администратора.
     * @param role роль пользователя
     * @param pageable параметры пагинации
     * @return страница с пользователями указанной роли
     */
    public Page<UserDTO> getAllUsersByRole(final Role role, final Pageable pageable) {
        return userRepository.findAllByRole(role, pageable).map(UserMapper::toUserDTO);
    }

    /**
     * Удаление пользователя по username. Функция для администратора.
     *
     * @param id ID пользователя
     * @throws UserNotFound если пользователь не найден
     */
    @Transactional
    public void deleteUser(final UUID id) {
        log.info("Deleting user with id {}", id);

        final User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFound("User with id " + id + " not found"));

        userRepository.delete(user);
        log.info("User with id {} deleted successfully", id);
    }

    public boolean existsById(final UUID userId) {
        return userRepository.existsById(userId);
    }
}
