package com.acme.salary.reports.service;

import com.acme.salary.common.response.PageResponse;
import com.acme.salary.employee.entity.Employee;
import com.acme.salary.reports.dto.SalaryReportRowDto;
import com.acme.salary.reports.util.CsvSanitizer;
import com.acme.salary.salary.entity.SalaryRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public PageResponse<SalaryReportRowDto> getSalaryReport(
            Long departmentId,
            Long countryId,
            String currency,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String status,
            LocalDate modifiedFrom,
            LocalDate modifiedTo,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // 1. Count Query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SalaryRecord> countRoot = countQuery.from(SalaryRecord.class);
        Join<SalaryRecord, Employee> countEmpJoin = countRoot.join("employee");

        List<Predicate> countPredicates = buildPredicates(
                cb, countRoot, countEmpJoin, departmentId, countryId, currency, minSalary, maxSalary, status, modifiedFrom, modifiedTo
        );
        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        if (total == 0) {
            return PageResponse.from(Page.empty(pageable));
        }

        // 2. Data Query
        CriteriaQuery<SalaryRecord> dataQuery = cb.createQuery(SalaryRecord.class);
        Root<SalaryRecord> root = dataQuery.from(SalaryRecord.class);
        Join<SalaryRecord, Employee> empJoin = root.join("employee");
        root.fetch("employee").fetch("department");
        root.fetch("employee").fetch("country");
        root.fetch("employee").fetch("jobTitle");
        root.fetch("currency");

        List<Predicate> predicates = buildPredicates(
                cb, root, empJoin, departmentId, countryId, currency, minSalary, maxSalary, status, modifiedFrom, modifiedTo
        );

        dataQuery.select(root).where(predicates.toArray(new Predicate[0]));
        dataQuery.orderBy(cb.asc(empJoin.get("lastName")), cb.asc(empJoin.get("firstName")));

        TypedQuery<SalaryRecord> query = entityManager.createQuery(dataQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<SalaryReportRowDto> dtos = query.getResultList().stream()
                .map(this::mapToRowDto)
                .toList();

        Page<SalaryReportRowDto> page = new PageImpl<>(dtos, pageable, total);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public void exportSalaryReportCsv(
            Long departmentId,
            Long countryId,
            String currency,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String status,
            LocalDate modifiedFrom,
            LocalDate modifiedTo,
            OutputStream outputStream) throws IOException {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SalaryRecord> dataQuery = cb.createQuery(SalaryRecord.class);
        Root<SalaryRecord> root = dataQuery.from(SalaryRecord.class);
        Join<SalaryRecord, Employee> empJoin = root.join("employee");
        root.fetch("employee").fetch("department");
        root.fetch("employee").fetch("country");
        root.fetch("employee").fetch("jobTitle");
        root.fetch("currency");

        List<Predicate> predicates = buildPredicates(
                cb, root, empJoin, departmentId, countryId, currency, minSalary, maxSalary, status, modifiedFrom, modifiedTo
        );

        dataQuery.select(root).where(predicates.toArray(new Predicate[0]));
        dataQuery.orderBy(cb.asc(empJoin.get("lastName")), cb.asc(empJoin.get("firstName")));

        TypedQuery<SalaryRecord> query = entityManager.createQuery(dataQuery);

        // Stream and flush directly to response stream
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), CSVFormat.DEFAULT)) {
            // Write CSV Header
            printer.printRecord(
                    "Employee Code",
                    "Employee Name",
                    "Email",
                    "Department",
                    "Country",
                    "Job Title",
                    "Status",
                    "Hire Date",
                    "Salary Amount",
                    "Currency",
                    "Pay Frequency",
                    "Annualized Salary (Est)",
                    "Effective From",
                    "Effective To"
            );

            // Fetch and stream in chunks of 500 to keep memory strictly bounded
            int pageSize = 500;
            int offset = 0;
            boolean hasMore = true;

            while (hasMore) {
                query.setFirstResult(offset);
                query.setMaxResults(pageSize);
                List<SalaryRecord> batch = query.getResultList();

                for (SalaryRecord s : batch) {
                    Employee e = s.getEmployee();
                    BigDecimal annualized = SalaryReportRowDto.computeAnnualized(s.getAmount(), s.getPayFrequency());

                    printer.printRecord(
                            CsvSanitizer.sanitize(e.getEmployeeCode()),
                            CsvSanitizer.sanitize(e.getFirstName() + " " + e.getLastName()),
                            CsvSanitizer.sanitize(e.getEmail()),
                            CsvSanitizer.sanitize(e.getDepartment().getName()),
                            CsvSanitizer.sanitize(e.getCountry().getName()),
                            CsvSanitizer.sanitize(e.getJobTitle().getTitle()),
                            CsvSanitizer.sanitize(e.getStatus()),
                            CsvSanitizer.sanitize(e.getHireDate()),
                            CsvSanitizer.sanitize(s.getAmount()),
                            CsvSanitizer.sanitize(s.getCurrency().getCode()),
                            CsvSanitizer.sanitize(s.getPayFrequency()),
                            CsvSanitizer.sanitize(annualized),
                            CsvSanitizer.sanitize(s.getEffectiveFrom()),
                            CsvSanitizer.sanitize(s.getEffectiveTo() != null ? s.getEffectiveTo() : "Current")
                    );
                }

                printer.flush();
                offset += batch.size();
                hasMore = batch.size() == pageSize;
                entityManager.clear(); // Detach to release 1st-level cache memory
            }
        }
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<SalaryRecord> root,
            Join<SalaryRecord, Employee> empJoin,
            Long departmentId,
            Long countryId,
            String currency,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String status,
            LocalDate modifiedFrom,
            LocalDate modifiedTo) {

        List<Predicate> predicates = new ArrayList<>();

        // By default, query active current salaries unless date range specified
        if (modifiedFrom == null && modifiedTo == null) {
            predicates.add(cb.isNull(root.get("effectiveTo")));
        } else {
            if (modifiedFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("effectiveFrom"), modifiedFrom));
            }
            if (modifiedTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("effectiveFrom"), modifiedTo));
            }
        }

        if (status != null && !status.isBlank()) {
            predicates.add(cb.equal(cb.upper(empJoin.get("status")), status.trim().toUpperCase()));
        }

        if (departmentId != null) {
            predicates.add(cb.equal(empJoin.get("department").get("id"), departmentId));
        }

        if (countryId != null) {
            predicates.add(cb.equal(empJoin.get("country").get("id"), countryId));
        }

        if (currency != null && !currency.isBlank()) {
            predicates.add(cb.equal(cb.upper(root.get("currency").get("code")), currency.trim().toUpperCase()));
        }

        if (minSalary != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minSalary));
        }

        if (maxSalary != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxSalary));
        }

        return predicates;
    }

    private SalaryReportRowDto mapToRowDto(SalaryRecord s) {
        Employee e = s.getEmployee();
        BigDecimal annualized = SalaryReportRowDto.computeAnnualized(s.getAmount(), s.getPayFrequency());
        return new SalaryReportRowDto(
                e.getId(),
                e.getEmployeeCode(),
                e.getFirstName() + " " + e.getLastName(),
                e.getEmail(),
                e.getDepartment().getName(),
                e.getCountry().getName(),
                e.getJobTitle().getTitle(),
                e.getStatus(),
                e.getHireDate(),
                s.getAmount(),
                s.getCurrency().getCode(),
                s.getCurrency().getSymbol(),
                s.getPayFrequency(),
                annualized,
                s.getEffectiveFrom(),
                s.getEffectiveTo()
        );
    }
}
