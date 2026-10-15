package com.acme.salary.analytics.dto;

import java.math.BigDecimal;

public record CurrencySalarySummaryDto(
        String currencyCode,
        String currencySymbol,
        long employeeCount,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        BigDecimal avgSalary
) {}
