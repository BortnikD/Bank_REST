package com.bortnik.bank_rest.dto.auth;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLogin {
    private String username;
    private String password;
}
