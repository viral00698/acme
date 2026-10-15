package com.acme.salary.reports.controller;

import com.acme.salary.common.response.ApiResponse;
import com.acme.salary.common.response.PageResponse;
import com.acme.salary.reports.dto.SalaryReportRowDto;
import com.acme.salary.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports & Exports", description = "Endpoints for tabular compensation reporting and sanitized CSV downloads")
@SecurityRequirement(name = "BearerAuth")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/salaries")
    @Operation(summary = "Query paginated salary reports with flexible multi-criteria filters")
    public ResponseEntity<ApiResponse<PageResponse<SalaryReportRowDto>>> getSalaryReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long countryId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate modifiedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate modifiedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int boundedSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(page, 0), boundedSize, Sort.by("id").ascending());

        PageResponse<SalaryReportRowDto> report = reportService.getSalaryReport(
                departmentId, countryId, currency, minSalary, maxSalary, status, modifiedFrom, modifiedTo, pageable
        );
        return ResponseEntity.ok(ApiResponse.ok(report));
    }

    @GetMapping("/salaries/export")
    @PreAuthorize("hasAnyAuthority('ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
    @Operation(summary = "Stream sanitized CSV export of filtered salary records")
    public ResponseEntity<StreamingResponseBody> exportSalaryReportCsv(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long countryId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate modifiedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate modifiedTo,
            HttpServletResponse response) {

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"acme_salaries_report.csv\"");

        StreamingResponseBody stream = outputStream -> {
            reportService.exportSalaryReportCsv(
                    departmentId, countryId, currency, minSalary, maxSalary, status, modifiedFrom, modifiedTo, outputStream
            );
        };

        return ResponseEntity.ok(stream);
    }
}
