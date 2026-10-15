package com.acme.salary.analytics.dto;

import java.math.BigDecimal;

public record SalaryBandDto(
        String bandName,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String currencyCode,
        long employeeCount
) {}
