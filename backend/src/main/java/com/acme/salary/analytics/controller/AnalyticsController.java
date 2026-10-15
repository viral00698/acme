package com.acme.salary.analytics.controller;

import com.acme.salary.analytics.dto.*;
import com.acme.salary.analytics.service.AnalyticsService;
import com.acme.salary.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics & Dashboard", description = "Endpoints for real-time compensation metrics and headcount analytics")
@SecurityRequirement(name = "BearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get high-level organizational and compensation overview metrics")
    public ResponseEntity<ApiResponse<DashboardOverviewDto>> getOverview() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getOverview()));
    }

    @GetMapping("/by-country")
    @Operation(summary = "Get active headcount and average salary by country")
    public ResponseEntity<ApiResponse<List<CountryAnalyticsDto>>> getByCountry() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getByCountry()));
    }

    @GetMapping("/by-department")
    @Operation(summary = "Get active headcount and average salary by department")
    public ResponseEntity<ApiResponse<List<DepartmentAnalyticsDto>>> getByDepartment() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getByDepartment()));
    }

    @GetMapping("/salary-distribution")
    @Operation(summary = "Get salary band counts for a specified currency")
    public ResponseEntity<ApiResponse<List<SalaryBandDto>>> getSalaryDistribution(
            @RequestParam(required = false, defaultValue = "USD") String currency) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getSalaryDistribution(currency)));
    }
}
