package com.acme.salary.salary.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSalaryRequest(
        @NotNull(message = "Salary amount is required")
        @DecimalMin(value = "0.01", message = "Salary must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Currency code is required")
        @Size(min = 3, max = 3, message = "Currency code must be 3 ISO characters")
        String currencyCode,

        @NotBlank(message = "Pay frequency is required")
        @Pattern(regexp = "^(ANNUAL|MONTHLY|BIWEEKLY)$", message = "Pay frequency must be ANNUAL, MONTHLY, or BIWEEKLY")
        String payFrequency,

        @NotNull(message = "Effective date is required")
        LocalDate effectiveFrom
) {}
