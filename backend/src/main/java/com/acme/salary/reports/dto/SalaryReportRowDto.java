package com.acme.salary.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalaryReportRowDto(
        Long employeeId,
        String employeeCode,
        String employeeName,
        String email,
        String department,
        String country,
        String jobTitle,
        String status,
        LocalDate hireDate,
        BigDecimal salaryAmount,
        String currencyCode,
        String currencySymbol,
        String payFrequency,
        BigDecimal annualizedSalary,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
    public static BigDecimal computeAnnualized(BigDecimal amount, String payFrequency) {
        if (amount == null) return BigDecimal.ZERO;
        if ("MONTHLY".equalsIgnoreCase(payFrequency)) {
            return amount.multiply(BigDecimal.valueOf(12));
        } else if ("BIWEEKLY".equalsIgnoreCase(payFrequency)) {
            return amount.multiply(BigDecimal.valueOf(26));
        }
        return amount; // ANNUAL
    }
}
