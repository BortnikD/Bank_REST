package com.bortnik.bank_rest.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLogin {
    private String username;
    private String password;
}
