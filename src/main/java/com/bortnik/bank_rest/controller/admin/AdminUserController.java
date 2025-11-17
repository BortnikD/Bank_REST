package com.bortnik.bank_rest.controller.admin;

import com.bortnik.bank_rest.dto.ApiResponse;
import com.bortnik.bank_rest.dto.user.UserDTO;
import com.bortnik.bank_rest.entity.Role;
import com.bortnik.bank_rest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "User management endpoints for administrators")
public class AdminUserController {

    private final UserService userService;

    @Operation(
            summary = "Get paginated list of all users",
            description = "Returns a paginated list of users. Available only for administrators."
    )
    @GetMapping()
    ApiResponse<Page<UserDTO>> getAllUsers(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @Parameter(description = "Filter by role")
            @RequestParam(required = false)
            Role role
    ) {
        if (role != null) {
            return ApiResponse.<Page<UserDTO>>builder()
                    .responseData(userService.getAllUsersByRole(role, pageable))
                    .build();
        }
        return ApiResponse.<Page<UserDTO>>builder()
                .responseData(userService.getAllUsers(pageable))
                .build();
    }

    @Operation(
            summary = "Get user by ID",
            description = "Returns detailed information about a user by their UUID."
    )
    @GetMapping("/{userId}")
    ApiResponse<UserDTO> getUserById(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId
    ) {
        return ApiResponse.<UserDTO>builder()
                .responseData(userService.getUserById(userId))
                .build();
    }

    @Operation(
            summary = "Grant ADMIN role to user",
            description = "Promotes a user by assigning them the ADMIN role."
    )
    @PostMapping("/{userId}/make-admin")
    ApiResponse<UserDTO> makeAdmin(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId
    ) {
        return ApiResponse.<UserDTO>builder()
                .responseData(userService.makeAdmin(userId))
                .build();
    }

    @Operation(
            summary = "Delete user",
            description = "Deletes a user by their UUID."
    )
    @DeleteMapping("/{userId}")
    ResponseEntity<Void> deleteUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
