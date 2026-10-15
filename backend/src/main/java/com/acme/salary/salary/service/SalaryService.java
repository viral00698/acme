package com.acme.salary.salary.service;

import com.acme.salary.audit.service.AuditService;
import com.acme.salary.auth.entity.UserAccount;
import com.acme.salary.auth.repository.UserAccountRepository;
import com.acme.salary.common.exception.ResourceNotFoundException;
import com.acme.salary.common.exception.SalaryOverlapException;
import com.acme.salary.employee.entity.Employee;
import com.acme.salary.employee.repository.EmployeeRepository;
import com.acme.salary.organization.entity.Currency;
import com.acme.salary.organization.repository.CurrencyRepository;
import com.acme.salary.salary.dto.CreateSalaryRequest;
import com.acme.salary.salary.dto.SalaryHistoryResponse;
import com.acme.salary.salary.dto.SalaryRecordDto;
import com.acme.salary.salary.entity.SalaryRecord;
import com.acme.salary.salary.repository.SalaryRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SalaryService {

    private final SalaryRecordRepository salaryRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrencyRepository currencyRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditService auditService;

    public SalaryService(
            SalaryRecordRepository salaryRecordRepository,
            EmployeeRepository employeeRepository,
            CurrencyRepository currencyRepository,
            UserAccountRepository userAccountRepository,
            AuditService auditService) {
        this.salaryRecordRepository = salaryRecordRepository;
        this.employeeRepository = employeeRepository;
        this.currencyRepository = currencyRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public SalaryHistoryResponse getSalaryHistory(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        List<SalaryRecord> records = salaryRecordRepository.findByEmployeeIdOrderByEffectiveFromDesc(employeeId);

        SalaryRecordDto currentSalary = records.stream()
                .filter(SalaryRecord::isActive)
                .findFirst()
                .map(this::mapToDto)
                .orElse(null);

        List<SalaryRecordDto> history = records.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        String fullName = employee.getFirstName() + " " + employee.getLastName();
        return new SalaryHistoryResponse(employee.getId(), employee.getEmployeeCode(), fullName, currentSalary, history);
    }

    @Transactional(readOnly = true)
    public SalaryRecordDto getSalaryRecord(Long employeeId, Long salaryId) {
        SalaryRecord record = salaryRecordRepository.findById(salaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary record not found with id: " + salaryId));

        if (!record.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException("Salary record " + salaryId + " does not belong to employee " + employeeId);
        }

        return mapToDto(record);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SalaryRecordDto createSalaryRevision(
            Long employeeId,
            CreateSalaryRequest request,
            String actorUsername,
            String clientIp) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Currency currency = currencyRepository.findById(request.currencyCode().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + request.currencyCode()));

        UserAccount actor = userAccountRepository.findByUsername(actorUsername).orElse(null);

        Optional<SalaryRecord> activeRecordOpt = salaryRecordRepository.findActiveByEmployeeId(employeeId);
        Long activeRecordId = -1L;
        Map<String, Object> auditDetails = new HashMap<>();

        if (activeRecordOpt.isPresent()) {
            SalaryRecord activeRecord = activeRecordOpt.get();
            activeRecordId = activeRecord.getId();

            // Validate that new effective date is strictly after current effective date
            if (!request.effectiveFrom().isAfter(activeRecord.getEffectiveFrom())) {
                throw new SalaryOverlapException(String.format(
                        "New salary effective start date (%s) must be after currently active salary effective date (%s)",
                        request.effectiveFrom(), activeRecord.getEffectiveFrom()
                ));
            }
        }

        // Verify no other overlapping historical/future records exist
        List<SalaryRecord> overlaps = salaryRecordRepository.findOverlappingRecords(
                employeeId, request.effectiveFrom(), null, activeRecordId
        );
        if (!overlaps.isEmpty()) {
            SalaryRecord conflict = overlaps.get(0);
            throw new SalaryOverlapException(String.format(
                    "Salary effective date (%s) overlaps with existing record (%s to %s)",
                    request.effectiveFrom(), conflict.getEffectiveFrom(),
                    conflict.getEffectiveTo() != null ? conflict.getEffectiveTo().toString() : "ongoing"
            ));
        }

        if (activeRecordOpt.isPresent()) {
            SalaryRecord activeRecord = activeRecordOpt.get();
            // Close active record: set effective_to to day before new start date
            LocalDate newEffectiveTo = request.effectiveFrom().minusDays(1);
            activeRecord.setEffectiveTo(newEffectiveTo);
            salaryRecordRepository.save(activeRecord);

            auditDetails.put("previousSalaryId", activeRecord.getId());
            auditDetails.put("previousAmount", activeRecord.getAmount());
            auditDetails.put("previousEffectiveTo", newEffectiveTo.toString());
        }

        // Insert new active salary record
        SalaryRecord newRecord = new SalaryRecord(
                employee,
                request.amount(),
                currency,
                request.payFrequency(),
                request.effectiveFrom(),
                null,
                actor
        );

        SalaryRecord saved = salaryRecordRepository.save(newRecord);

        // Audit Logging
        auditDetails.put("newSalaryId", saved.getId());
        auditDetails.put("newAmount", saved.getAmount());
        auditDetails.put("currency", currency.getCode());
        auditDetails.put("payFrequency", saved.getPayFrequency());
        auditDetails.put("effectiveFrom", saved.getEffectiveFrom().toString());

        auditService.recordEvent(
                actorUsername,
                "SALARY_RECORD",
                employeeId,
                activeRecordOpt.isPresent() ? "SALARY_REVISION" : "SALARY_CREATION",
                auditDetails,
                clientIp
        );

        return mapToDto(saved);
    }

    private SalaryRecordDto mapToDto(SalaryRecord s) {
        return new SalaryRecordDto(
                s.getId(),
                s.getEmployee().getId(),
                s.getAmount(),
                s.getCurrency().getCode(),
                s.getCurrency().getSymbol(),
                s.getPayFrequency(),
                s.getEffectiveFrom(),
                s.getEffectiveTo(),
                s.isActive(),
                s.getCreatedAt(),
                s.getCreatedBy() != null ? s.getCreatedBy().getUsername() : "SYSTEM"
        );
    }
}
