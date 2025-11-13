package com.bortnik.bank_rest.dto.auth;

import lombok.Data;

@Data
public class UserLogin {
    private String username;
    private String password;
}
