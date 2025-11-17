package com.bortnik.bank_rest.dto.user;

import com.bortnik.bank_rest.entity.Role;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserCreateDTO {
    String username;
    String password;
    Role role;
}
