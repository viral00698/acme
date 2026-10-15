package com.acme.salary.employee.dto;

import java.time.LocalDate;

public record EmployeeSummaryDto(
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
        LocalDate hireDate
) {}
