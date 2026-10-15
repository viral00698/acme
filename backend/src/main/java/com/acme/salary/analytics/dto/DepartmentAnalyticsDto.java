package com.acme.salary.analytics.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DepartmentAnalyticsDto(
        Long departmentId,
        String departmentCode,
        String departmentName,
        long employeeCount,
        Map<String, BigDecimal> avgSalaryByCurrency
) {}
