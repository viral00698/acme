package com.acme.salary.audit.service;

import com.acme.salary.audit.dto.AuditLogDto;
import com.acme.salary.audit.entity.AuditLog;
import com.acme.salary.audit.repository.AuditLogRepository;
import com.acme.salary.common.response.PageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void recordEvent(String actor, String entityName, Long entityId, String action, Object details, String clientIp) {
        String json = "{}";
        if (details != null) {
            try {
                json = objectMapper.writeValueAsString(details);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize audit details for entity {} id {}", entityName, entityId, e);
                json = "{\"error\": \"serialization_failed\"}";
            }
        }

        AuditLog auditLog = new AuditLog(actor, entityName, entityId, action, json, clientIp);
        auditLogRepository.save(auditLog);
        log.info("AUDIT EVENT: user={} action={} entity={} id={}", actor, action, entityName, entityId);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> getAuditLogs(String entityName, Long entityId, String actor, Pageable pageable) {
        Specification<AuditLog> spec = Specification.where(null);

        if (entityName != null && !entityName.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("entityName")), entityName.toUpperCase()));
        }
        if (entityId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityId"), entityId));
        }
        if (actor != null && !actor.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("actorUsername")), "%" + actor.toLowerCase() + "%"));
        }

        Page<AuditLogDto> page = auditLogRepository.findAll(spec, pageable).map(this::mapToDto);
        return PageResponse.from(page);
    }

    private AuditLogDto mapToDto(AuditLog entity) {
        return new AuditLogDto(
                entity.getId(),
                entity.getActorUsername(),
                entity.getEntityName(),
                entity.getEntityId(),
                entity.getAction(),
                entity.getDetailsJson(),
                entity.getClientIp(),
                entity.getCreatedAt()
        );
    }
}
