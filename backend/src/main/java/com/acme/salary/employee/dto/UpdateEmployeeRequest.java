package com.acme.salary.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateEmployeeRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        String phoneNumber,

        @NotNull(message = "Department is required")
        Long departmentId,

        @NotNull(message = "Country is required")
        Long countryId,

        @NotNull(message = "Job title is required")
        Long jobTitleId,

        @NotNull(message = "Hire date is required")
        LocalDate hireDate
) {}
