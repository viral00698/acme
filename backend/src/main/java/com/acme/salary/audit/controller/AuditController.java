package com.acme.salary.audit.controller;

import com.acme.salary.audit.dto.AuditLogDto;
import com.acme.salary.audit.service.AuditService;
import com.acme.salary.common.response.ApiResponse;
import com.acme.salary.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Trail", description = "Endpoints for inspecting immutable audit history (Restricted to HR_ADMIN)")
@SecurityRequirement(name = "BearerAuth")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_HR_ADMIN')")
    @Operation(summary = "Paginated query of system audit logs with optional entity and actor filters")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogDto>>> getAuditLogs(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String actor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int boundedSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(page, 0), boundedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        PageResponse<AuditLogDto> response = auditService.getAuditLogs(entityName, entityId, actor, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
