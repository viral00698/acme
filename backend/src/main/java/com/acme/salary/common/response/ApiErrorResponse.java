package com.acme.salary.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        List<FieldErrorDetail> fieldErrors
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(status, error, message, path, Instant.now(), null);
    }

    public static ApiErrorResponse of(int status, String error, String message, String path, List<FieldErrorDetail> fieldErrors) {
        return new ApiErrorResponse(status, error, message, path, Instant.now(), fieldErrors);
    }
}
