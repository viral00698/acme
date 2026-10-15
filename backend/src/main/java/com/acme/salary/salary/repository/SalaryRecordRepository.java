package com.acme.salary.salary.repository;

import com.acme.salary.salary.entity.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {

    @Query("SELECT s FROM SalaryRecord s " +
           "JOIN FETCH s.currency " +
           "LEFT JOIN FETCH s.createdBy " +
           "WHERE s.employee.id = :employeeId AND s.effectiveTo IS NULL")
    Optional<SalaryRecord> findActiveByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT s FROM SalaryRecord s " +
           "JOIN FETCH s.currency " +
           "LEFT JOIN FETCH s.createdBy " +
           "WHERE s.employee.id = :employeeId " +
           "ORDER BY s.effectiveFrom DESC")
    List<SalaryRecord> findByEmployeeIdOrderByEffectiveFromDesc(@Param("employeeId") Long employeeId);

    long countByEmployeeId(Long employeeId);

    @Query("SELECT s FROM SalaryRecord s " +
           "WHERE s.employee.id = :employeeId " +
           "AND s.id != :excludeId " +
           "AND (" +
           "  (:to IS NULL AND (s.effectiveTo IS NULL OR s.effectiveTo >= :from)) OR " +
           "  (:to IS NOT NULL AND s.effectiveFrom <= :to AND (s.effectiveTo IS NULL OR s.effectiveTo >= :from))" +
           ")")
    List<SalaryRecord> findOverlappingRecords(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("excludeId") Long excludeId);
}
