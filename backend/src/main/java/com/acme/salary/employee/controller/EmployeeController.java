package com.acme.salary.employee.controller;

import com.acme.salary.common.response.ApiResponse;
import com.acme.salary.common.response.PageResponse;
import com.acme.salary.employee.dto.*;
import com.acme.salary.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Management", description = "Endpoints for employee directory search, creation, and profile lifecycle")
@SecurityRequirement(name = "BearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "Search and filter employee directory with server-side pagination")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeSummaryDto>>> searchEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long countryId,
            @RequestParam(required = false) Long jobTitleId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        int boundedSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), boundedSize, Sort.by(direction, sortBy));

        PageResponse<EmployeeSummaryDto> response = employeeService.searchEmployees(
                search, departmentId, countryId, jobTitleId, status, pageable
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full employee profile including active salary details")
    public ResponseEntity<ApiResponse<EmployeeDetailDto>> getEmployee(@PathVariable Long id) {
        EmployeeDetailDto employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(ApiResponse.ok(employee));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
    @Operation(summary = "Create a new employee profile")
    public ResponseEntity<ApiResponse<EmployeeDetailDto>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest servletRequest) {

        String actor = userDetails != null ? userDetails.getUsername() : "SYSTEM";
        String clientIp = getClientIp(servletRequest);

        EmployeeDetailDto created = employeeService.createEmployee(request, actor, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Employee created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
    @Operation(summary = "Update employee profile details")
    public ResponseEntity<ApiResponse<EmployeeDetailDto>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest servletRequest) {

        String actor = userDetails != null ? userDetails.getUsername() : "SYSTEM";
        String clientIp = getClientIp(servletRequest);

        EmployeeDetailDto updated = employeeService.updateEmployee(id, request, actor, clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Employee updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_HR_ADMIN', 'ROLE_HR_MANAGER')")
    @Operation(summary = "Update employee employment status (ACTIVE, INACTIVE, TERMINATED, ON_LEAVE)")
    public ResponseEntity<ApiResponse<EmployeeDetailDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest servletRequest) {

        String actor = userDetails != null ? userDetails.getUsername() : "SYSTEM";
        String clientIp = getClientIp(servletRequest);

        EmployeeDetailDto updated = employeeService.updateStatus(id, request, actor, clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Employee status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_HR_ADMIN')")
    @Operation(summary = "Delete an employee (permitted only if no financial or audit history exists)")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest servletRequest) {

        String actor = userDetails != null ? userDetails.getUsername() : "SYSTEM";
        String clientIp = getClientIp(servletRequest);

        employeeService.deleteEmployee(id, actor, clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Employee removed successfully", null));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
