package com.acme.salary.audit.dto;

import java.time.OffsetDateTime;

public record AuditLogDto(
        Long id,
        String actorUsername,
        String entityName,
        Long entityId,
        String action,
        String detailsJson,
        String clientIp,
        OffsetDateTime createdAt
) {}
