package com.bortnik.bank_rest.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiResponse<T> {
    @Builder.Default
    boolean success = true;
    T responseData;
    ApiError apiError;
}
