package com.acme.salary.analytics.dto;

import java.math.BigDecimal;
import java.util.Map;

public record CountryAnalyticsDto(
        Long countryId,
        String countryCode,
        String countryName,
        long employeeCount,
        Map<String, BigDecimal> avgSalaryByCurrency
) {}
