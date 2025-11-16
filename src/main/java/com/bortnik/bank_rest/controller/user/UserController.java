package com.bortnik.bank_rest.controller.user;

import com.bortnik.bank_rest.dto.ApiError;
import com.bortnik.bank_rest.dto.user.UserDTO;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get information about current user",
            description = "Returns the profile of the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User information retrieved",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/who-am-i")
    UserDTO whoAmI(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return userService.getUserById(user.getId());
    }
}
