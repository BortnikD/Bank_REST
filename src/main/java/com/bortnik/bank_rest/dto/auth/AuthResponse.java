package com.bortnik.bank_rest.dto.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String tokenType;
    String token;
    String username;
}