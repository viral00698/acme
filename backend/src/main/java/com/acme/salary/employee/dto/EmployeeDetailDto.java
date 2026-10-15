package com.acme.salary.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record EmployeeDetailDto(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Long departmentId,
        String departmentName,
        Long countryId,
        String countryName,
        String countryCode,
        Long jobTitleId,
        String jobTitle,
        String status,
        LocalDate hireDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        // Active Salary Info (nullable if not yet set)
        BigDecimal currentSalaryAmount,
        String currentSalaryCurrency,
        String currentSalarySymbol,
        String currentPayFrequency,
        LocalDate currentSalaryEffectiveFrom
) {}
