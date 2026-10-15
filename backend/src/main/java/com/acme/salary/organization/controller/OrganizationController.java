package com.acme.salary.organization.controller;

import com.acme.salary.common.response.ApiResponse;
import com.acme.salary.organization.dto.*;
import com.acme.salary.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organization")
@Tag(name = "Organization & Reference Data", description = "Endpoints for countries, departments, currencies, and job titles")
@SecurityRequirement(name = "BearerAuth")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/departments")
    @Operation(summary = "List all company departments")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(organizationService.getAllDepartments()));
    }

    @GetMapping("/countries")
    @Operation(summary = "List all operational countries")
    public ResponseEntity<ApiResponse<List<CountryDto>>> getCountries() {
        return ResponseEntity.ok(ApiResponse.ok(organizationService.getAllCountries()));
    }

    @GetMapping("/currencies")
    @Operation(summary = "List all supported currencies")
    public ResponseEntity<ApiResponse<List<CurrencyDto>>> getCurrencies() {
        return ResponseEntity.ok(ApiResponse.ok(organizationService.getAllCurrencies()));
    }

    @GetMapping("/job-titles")
    @Operation(summary = "List job titles (optionally filtered by department)")
    public ResponseEntity<ApiResponse<List<JobTitleDto>>> getJobTitles(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok(organizationService.getAllJobTitles(departmentId)));
    }
}
