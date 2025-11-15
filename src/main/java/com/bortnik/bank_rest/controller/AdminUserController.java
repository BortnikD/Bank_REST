package com.bortnik.bank_rest.controller;

import com.bortnik.bank_rest.dto.ApiError;
import com.bortnik.bank_rest.dto.user.UserDTO;
import com.bortnik.bank_rest.entity.Role;
import com.bortnik.bank_rest.security.services.UserDetailsImpl;
import com.bortnik.bank_rest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "Get paginated list of all users",
            description = "Returns a paginated list of users. Available only for administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful retrieval",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
    })
    @GetMapping()
    Page<UserDTO> getAllUsers(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(
                    size = 20,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @Parameter(description = "Filter by role")
            @RequestParam(required = false)
            Role role
    ) {
        if (role != null) {
            return userService.getAllUsersByRole(role, pageable);
        }
        return userService.getAllUsers(pageable);
    }

    @Operation(
            summary = "Get user by ID",
            description = "Returns detailed information about a user by their UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{userId}")
    UserDTO getUserById(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId
    ) {
        return userService.getUserById(userId);
    }

    @Operation(
            summary = "Get information about current admin",
            description = "Returns the profile of the authenticated administrator."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User information retrieved",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
    })
    @GetMapping("/who-am-i")
    UserDTO whoAmI(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return userService.getUserById(user.getId());
    }

    @Operation(
            summary = "Grant ADMIN role to user",
            description = "Promotes a user by assigning them the ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User promoted",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/{userId}/make-admin")
    UserDTO makeAdmin(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId
    ) {
        return userService.makeAdmin(userId);
    }

    @Operation(
            summary = "Delete user",
            description = "Deletes a user by their UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping("/{userId}")
    ResponseEntity<Void> deleteUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
