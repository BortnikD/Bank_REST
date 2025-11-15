package com.bortnik.bank_rest.util.mappers;

import com.bortnik.bank_rest.dto.user.UserDTO;
import com.bortnik.bank_rest.entity.User;

public class UserMapper {
    public static UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .role(user.getRole())
                .username(user.getUsername())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
