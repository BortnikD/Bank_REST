package com.bortnik.bank_rest.service;

import com.bortnik.bank_rest.dto.user.UserCreateDTO;
import com.bortnik.bank_rest.dto.user.UserDTO;
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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Создание нового пользователя. Функция для администратора или сервиса аунтефикаций.
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
     * @param username имя пользователя
     * @return {@code UserDTO} информация о удалённом пользователе
     * @throws UserNotFound если пользователь не найден
     */
    @Transactional
    public UserDTO deleteUser(final String username) {
        final User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFound("User with username " + username + " not found"));
        userRepository.delete(user);
        return UserMapper.toUserDTO(user);
    }
}
