package com.bortnik.bank_rest.controller;

import com.bortnik.bank_rest.dto.user.UserDTO;
import com.bortnik.bank_rest.security.services.UserDetailsImpl;
import com.bortnik.bank_rest.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Users", description = "User management endpoints for administrators")
public class AdminUserController {

    private final UserService userService;

    @GetMapping()
    Page<UserDTO> getAllUsers(
            @PageableDefault(
                    size = 20,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return userService.getAllUsers(pageable);
    }

    @GetMapping("/{userId}")
    UserDTO getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/who-am-i")
    UserDTO whoAmI(@AuthenticationPrincipal UserDetailsImpl user) {
        return userService.getUserById(user.getId());
    }

    @PostMapping("/{userId}/make_admin")
    UserDTO makeAdmin(@PathVariable UUID userId) {
        return userService.makeAdmin(userId);
    }

    @DeleteMapping("/{userId}")
    ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build() ;
    }
}
