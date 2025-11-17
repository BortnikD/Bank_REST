package com.bortnik.bank_rest.dto.user;

import com.bortnik.bank_rest.entity.Role;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class UserDTO {
     UUID id;
     String username;
     Role role;
     LocalDateTime createdAt;
     LocalDateTime updatedAt;
}
