package com.acme.salary.audit.repository;

import com.acme.salary.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByEntityNameAndEntityIdOrderByCreatedAtDesc(String entityName, Long entityId);

    Page<AuditLog> findByOrderByCreatedAtDesc(Pageable pageable);
}
