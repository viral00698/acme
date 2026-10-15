package com.acme.salary.organization.dto;

public record JobTitleDto(
        Long id,
        String title,
        Long departmentId,
        String departmentName
) {}
