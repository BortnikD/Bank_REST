package com.bortnik.bank_rest.dto.user;

import com.bortnik.bank_rest.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreateDTO {
    private String username;
    private String password;
    private Role role;
}
