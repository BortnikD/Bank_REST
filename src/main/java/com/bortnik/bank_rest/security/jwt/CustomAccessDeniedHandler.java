package com.bortnik.bank_rest.security.jwt;

import com.bortnik.bank_rest.dto.ApiError;
import com.bortnik.bank_rest.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        final ApiError apiError = ApiError.builder()
                .error("Forbidden")
                .status(HttpStatus.FORBIDDEN)
                .timestamp(LocalDateTime.now())
                .message(accessDeniedException.getMessage())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.<ApiError>builder()
                    .success(false)
                    .apiError(apiError)
                    .build()
        ));
        response.getWriter().flush();
    }
}