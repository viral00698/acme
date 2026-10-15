package com.acme.salary.common.response;

public record FieldErrorDetail(
        String field,
        String rejectedValue,
        String message
) {}
