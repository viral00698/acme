package com.acme.salary.analytics.dto;

import java.util.List;

public record DashboardOverviewDto(
        long totalActiveEmployees,
        long totalDepartments,
        long totalCountries,
        List<CurrencySalarySummaryDto> currencySummaries
) {}
