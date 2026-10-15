package com.acme.salary.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CreateEmployeeRequest(
        @NotBlank(message = "Employee code is required")
        @Pattern(regexp = "^[A-Za-z0-9-_]{3,20}$", message = "Employee code must be 3-20 alphanumeric characters")
        String employeeCode,

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
