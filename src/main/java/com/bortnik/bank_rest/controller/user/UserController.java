package com.bortnik.bank_rest.controller.user;

import com.bortnik.bank_rest.dto.ApiResponse;
import com.bortnik.bank_rest.dto.user.UserDTO;
import com.bortnik.bank_rest.security.services.UserDetailsImpl;
import com.bortnik.bank_rest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @GetMapping("/who-am-i")
    ApiResponse<UserDTO> whoAmI(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return ApiResponse.<UserDTO>builder()
                .responseData(userService.getUserById(user.getId()))
                .build();
    }
}
