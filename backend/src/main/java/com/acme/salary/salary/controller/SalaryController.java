package com.acme.salary.salary.controller;

import com.acme.salary.common.response.ApiResponse;
import com.acme.salary.salary.dto.CreateSalaryRequest;
import com.acme.salary.salary.dto.SalaryHistoryResponse;
import com.acme.salary.salary.dto.SalaryRecordDto;
import com.acme.salary.salary.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/salaries")
@Tag(name = "Salary Management", description = "Endpoints for employee compensation, effective dates, and revision history")
@SecurityRequirement(name = "BearerAuth")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @GetMapping
    @Operation(summary = "Get full effective-dated salary history for an employee")
    public ResponseEntity<ApiResponse<SalaryHistoryResponse>> getSalaryHistory(
            @PathVariable Long employeeId) {
        SalaryHistoryResponse response = salaryService.getSalaryHistory(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{salaryId}")
    @Operation(summary = "Get specific salary record by ID")
    public ResponseEntity<ApiResponse<SalaryRecordDto>> getSalaryRecord(
            @PathVariable Long employeeId,
            @PathVariable Long salaryId) {
        SalaryRecordDto record = salaryService.getSalaryRecord(employeeId, salaryId);
        return ResponseEntity.ok(ApiResponse.ok(record));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
    @Operation(summary = "Assign initial compensation or execute effective-dated salary revision")
    public ResponseEntity<ApiResponse<SalaryRecordDto>> createSalaryRevision(
            @PathVariable Long employeeId,
            @Valid @RequestBody CreateSalaryRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest servletRequest) {

        String actor = userDetails != null ? userDetails.getUsername() : "SYSTEM";
        String clientIp = getClientIp(servletRequest);

        SalaryRecordDto created = salaryService.createSalaryRevision(employeeId, request, actor, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Salary revision processed successfully", created)
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
