package com.acme.salary.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateStatusRequest(
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "^(ACTIVE|INACTIVE|TERMINATED|ON_LEAVE)$", message = "Status must be ACTIVE, INACTIVE, TERMINATED, or ON_LEAVE")
        String status
) {}
