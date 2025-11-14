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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
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
        if (userRepository.existsByUsername(userCreateDTO.getUsername())) {
            throw new UserAlreadyExists("User with username " + userCreateDTO.getUsername() + " already exists");
        }
        return UserMapper.toUserDTO(
                userRepository.save(
                        User.builder()
                                .username(userCreateDTO.getUsername())
                                .password(userCreateDTO.getPassword())
                                .role(userCreateDTO.getRole())
                                .build()
                )
        );
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("User with id " + userId + " not found"));
        user.setRole(Role.ADMIN);
        return UserMapper.toUserDTO(userRepository.save(user));
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
     * Удаление пользователя по username. Функция для администратора.
     *
     * @param id ID пользователя
     * @throws UserNotFound если пользователь не найден
     */
    @Transactional
    public void deleteUser(final UUID id) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFound("User with id " + id + " not found"));
        userRepository.delete(user);
        UserMapper.toUserDTO(user);
    }

    public boolean existsById(final UUID userId) {
        return userRepository.existsById(userId);
    }
}
