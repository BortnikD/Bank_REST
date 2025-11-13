package com.bortnik.bank_rest.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String tokenType;
    private String token;
    private String username;
}