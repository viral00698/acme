package com.acme.salary.salary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record SalaryRecordDto(
        Long id,
        Long employeeId,
        BigDecimal amount,
        String currencyCode,
        String currencySymbol,
        String payFrequency,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active,
        OffsetDateTime createdAt,
        String createdByUsername
) {}
