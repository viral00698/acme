package com.acme.salary.analytics.repository;

import com.acme.salary.salary.entity.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<SalaryRecord, Long> {

    @Query("SELECT s.currency.code, s.currency.symbol, COUNT(s), MIN(s.amount), MAX(s.amount), AVG(s.amount) " +
           "FROM SalaryRecord s " +
           "JOIN s.employee e " +
           "WHERE s.effectiveTo IS NULL AND e.status = 'ACTIVE' " +
           "GROUP BY s.currency.code, s.currency.symbol")
    List<Object[]> getCurrencySummaries();

    @Query("SELECT e.country.id, e.country.code, e.country.name, COUNT(e) " +
           "FROM Employee e " +
           "WHERE e.status = 'ACTIVE' " +
           "GROUP BY e.country.id, e.country.code, e.country.name " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> getCountryHeadcounts();

    @Query("SELECT e.country.id, s.currency.code, AVG(s.amount) " +
           "FROM SalaryRecord s " +
           "JOIN s.employee e " +
           "WHERE s.effectiveTo IS NULL AND e.status = 'ACTIVE' " +
           "GROUP BY e.country.id, s.currency.code")
    List<Object[]> getCountryAvgSalaries();

    @Query("SELECT e.department.id, e.department.code, e.department.name, COUNT(e) " +
           "FROM Employee e " +
           "WHERE e.status = 'ACTIVE' " +
           "GROUP BY e.department.id, e.department.code, e.department.name " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> getDepartmentHeadcounts();

    @Query("SELECT e.department.id, s.currency.code, AVG(s.amount) " +
           "FROM SalaryRecord s " +
           "JOIN s.employee e " +
           "WHERE s.effectiveTo IS NULL AND e.status = 'ACTIVE' " +
           "GROUP BY e.department.id, s.currency.code")
    List<Object[]> getDepartmentAvgSalaries();

    @Query("SELECT COUNT(s) " +
           "FROM SalaryRecord s " +
           "JOIN s.employee e " +
           "WHERE s.effectiveTo IS NULL AND e.status = 'ACTIVE' " +
           "AND s.currency.code = :currency " +
           "AND s.amount >= :min AND (:max IS NULL OR s.amount < :max)")
    long countEmployeesInSalaryBand(
            @Param("currency") String currency,
            @Param("min") BigDecimal min,
            @Param("max") BigDecimal max);
}
