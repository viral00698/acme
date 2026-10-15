package com.acme.salary.salary.service;

import com.acme.salary.audit.service.AuditService;
import com.acme.salary.auth.entity.UserAccount;
import com.acme.salary.auth.repository.UserAccountRepository;
import com.acme.salary.common.exception.SalaryOverlapException;
import com.acme.salary.employee.entity.Employee;
import com.acme.salary.employee.repository.EmployeeRepository;
import com.acme.salary.organization.entity.Country;
import com.acme.salary.organization.entity.Currency;
import com.acme.salary.organization.entity.Department;
import com.acme.salary.organization.entity.JobTitle;
import com.acme.salary.organization.repository.CurrencyRepository;
import com.acme.salary.salary.dto.CreateSalaryRequest;
import com.acme.salary.salary.dto.SalaryRecordDto;
import com.acme.salary.salary.entity.SalaryRecord;
import com.acme.salary.salary.repository.SalaryRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

    @Mock
    private SalaryRecordRepository salaryRecordRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private AuditService auditService;

    private SalaryService salaryService;

    private Employee employee;
    private Currency usd;
    private UserAccount manager;

    @BeforeEach
    void setUp() {
        salaryService = new SalaryService(
                salaryRecordRepository,
                employeeRepository,
                currencyRepository,
                userAccountRepository,
                auditService
        );

        Department dept = new Department("ENG", "Engineering");
        Country country = new Country("USA", "United States", "USD");
        JobTitle title = new JobTitle("Software Engineer", dept);

        employee = new Employee("EMP-001", "John", "Doe", "john@acme.corp", null, dept, country, title, "ACTIVE", LocalDate.of(2023, 1, 1));
        employee.setId(1L);

        usd = new Currency("USD", "US Dollar", "$");
        manager = new UserAccount("manager", "manager@acme.corp", "hash", true);
    }

    @Test
    @DisplayName("Create initial salary record when employee has no prior salary")
    void testCreateInitialSalarySuccess() {
        CreateSalaryRequest request = new CreateSalaryRequest(
                new BigDecimal("95000.00"), "USD", "ANNUAL", LocalDate.of(2025, 1, 1)
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(currencyRepository.findById("USD")).thenReturn(Optional.of(usd));
        when(userAccountRepository.findByUsername("manager")).thenReturn(Optional.of(manager));
        when(salaryRecordRepository.findActiveByEmployeeId(1L)).thenReturn(Optional.empty());
        when(salaryRecordRepository.findOverlappingRecords(1L, request.effectiveFrom(), null, -1L))
                .thenReturn(Collections.emptyList());

        SalaryRecord saved = new SalaryRecord(employee, request.amount(), usd, request.payFrequency(), request.effectiveFrom(), null, manager);
        saved.setId(10L);
        when(salaryRecordRepository.save(any(SalaryRecord.class))).thenReturn(saved);

        SalaryRecordDto result = salaryService.createSalaryRevision(1L, request, "manager", "127.0.0.1");

        assertNotNull(result);
        assertEquals(new BigDecimal("95000.00"), result.amount());
        assertEquals("USD", result.currencyCode());
        assertTrue(result.active());
        assertNull(result.effectiveTo());
        verify(auditService).recordEvent(eq("manager"), eq("SALARY_RECORD"), eq(1L), eq("SALARY_CREATION"), any(), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("Salary revision closes currently active record and creates new active record")
    void testSalaryRevisionClosesPriorRecord() {
        SalaryRecord current = new SalaryRecord(employee, new BigDecimal("80000.00"), usd, "ANNUAL", LocalDate.of(2024, 1, 1), null, manager);
        current.setId(5L);

        CreateSalaryRequest request = new CreateSalaryRequest(
                new BigDecimal("100000.00"), "USD", "ANNUAL", LocalDate.of(2025, 1, 1)
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(currencyRepository.findById("USD")).thenReturn(Optional.of(usd));
        when(userAccountRepository.findByUsername("manager")).thenReturn(Optional.of(manager));
        when(salaryRecordRepository.findActiveByEmployeeId(1L)).thenReturn(Optional.of(current));
        when(salaryRecordRepository.findOverlappingRecords(1L, request.effectiveFrom(), null, 5L))
                .thenReturn(Collections.emptyList());

        SalaryRecord newSaved = new SalaryRecord(employee, request.amount(), usd, request.payFrequency(), request.effectiveFrom(), null, manager);
        newSaved.setId(6L);
        when(salaryRecordRepository.save(any(SalaryRecord.class))).thenReturn(newSaved);

        SalaryRecordDto result = salaryService.createSalaryRevision(1L, request, "manager", "127.0.0.1");

        assertNotNull(result);
        assertEquals(new BigDecimal("100000.00"), result.amount());

        // Verify active record was closed with effectiveTo = 2024-12-31
        assertEquals(LocalDate.of(2024, 12, 31), current.getEffectiveTo());
        verify(salaryRecordRepository, times(2)).save(any(SalaryRecord.class));
        verify(auditService).recordEvent(eq("manager"), eq("SALARY_RECORD"), eq(1L), eq("SALARY_REVISION"), any(), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("Reject salary revision with effective date on or before current effective date")
    void testRejectOverlappingEffectiveDate() {
        SalaryRecord current = new SalaryRecord(employee, new BigDecimal("80000.00"), usd, "ANNUAL", LocalDate.of(2025, 1, 1), null, manager);
        current.setId(5L);

        CreateSalaryRequest invalidRequest = new CreateSalaryRequest(
                new BigDecimal("90000.00"), "USD", "ANNUAL", LocalDate.of(2024, 6, 1) // Before 2025-01-01!
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(currencyRepository.findById("USD")).thenReturn(Optional.of(usd));
        when(userAccountRepository.findByUsername("manager")).thenReturn(Optional.of(manager));
        when(salaryRecordRepository.findActiveByEmployeeId(1L)).thenReturn(Optional.of(current));

        assertThrows(SalaryOverlapException.class, () ->
                salaryService.createSalaryRevision(1L, invalidRequest, "manager", "127.0.0.1")
        );

        verify(salaryRecordRepository, never()).save(any());
    }
}
