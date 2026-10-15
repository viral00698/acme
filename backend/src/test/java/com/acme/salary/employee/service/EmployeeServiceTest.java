package com.acme.salary.employee.service;

import com.acme.salary.audit.service.AuditService;
import com.acme.salary.common.exception.DuplicateResourceException;
import com.acme.salary.common.exception.InvalidOperationException;
import com.acme.salary.employee.dto.CreateEmployeeRequest;
import com.acme.salary.employee.dto.EmployeeDetailDto;
import com.acme.salary.employee.entity.Employee;
import com.acme.salary.employee.repository.EmployeeRepository;
import com.acme.salary.organization.entity.Country;
import com.acme.salary.organization.entity.Department;
import com.acme.salary.organization.entity.JobTitle;
import com.acme.salary.organization.repository.CountryRepository;
import com.acme.salary.organization.repository.DepartmentRepository;
import com.acme.salary.organization.repository.JobTitleRepository;
import com.acme.salary.salary.repository.SalaryRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private JobTitleRepository jobTitleRepository;
    @Mock
    private SalaryRecordRepository salaryRecordRepository;
    @Mock
    private AuditService auditService;

    private EmployeeService employeeService;

    private Department dept;
    private Country country;
    private JobTitle title;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(
                employeeRepository,
                departmentRepository,
                countryRepository,
                jobTitleRepository,
                salaryRecordRepository,
                auditService
        );

        dept = new Department("ENG", "Engineering");
        dept.setId(1L);
        country = new Country("USA", "United States", "USD");
        country.setId(1L);
        title = new JobTitle("Software Engineer", dept);
        title.setId(1L);
    }

    @Test
    @DisplayName("Create employee succeeds when code and email are unique")
    void testCreateEmployeeSuccess() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "EMP-00100", "Alice", "Smith", "alice.smith@acme.corp",
                "+1-555-0199", 1L, 1L, 1L, LocalDate.of(2025, 1, 15)
        );

        when(employeeRepository.existsByEmployeeCode("EMP-00100")).thenReturn(false);
        when(employeeRepository.existsByEmail("alice.smith@acme.corp")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(jobTitleRepository.findById(1L)).thenReturn(Optional.of(title));

        Employee saved = new Employee(
                request.employeeCode(), request.firstName(), request.lastName(),
                request.email(), request.phoneNumber(), dept, country, title,
                "ACTIVE", request.hireDate()
        );
        saved.setId(100L);

        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        EmployeeDetailDto result = employeeService.createEmployee(request, "manager", "127.0.0.1");

        assertNotNull(result);
        assertEquals("EMP-00100", result.employeeCode());
        assertEquals("Alice", result.firstName());
        assertEquals("Engineering", result.departmentName());
        verify(auditService).recordEvent(eq("manager"), eq("EMPLOYEE"), eq(100L), eq("CREATE"), any(), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("Create employee rejects duplicate employee code")
    void testCreateEmployeeDuplicateCode() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "EMP-00100", "Alice", "Smith", "alice@acme.corp",
                null, 1L, 1L, 1L, LocalDate.now()
        );

        when(employeeRepository.existsByEmployeeCode("EMP-00100")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                employeeService.createEmployee(request, "manager", "127.0.0.1")
        );
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete employee with existing compensation records throws InvalidOperationException")
    void testDeleteEmployeeWithSalaryRecordsFails() {
        Employee employee = new Employee("EMP-001", "Bob", "Jones", "bob@acme.corp", null, dept, country, title, "ACTIVE", LocalDate.now());
        employee.setId(10L);

        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(salaryRecordRepository.countByEmployeeId(10L)).thenReturn(3L);

        assertThrows(InvalidOperationException.class, () ->
                employeeService.deleteEmployee(10L, "admin", "127.0.0.1")
        );
        verify(employeeRepository, never()).delete(any(Employee.class));
    }
}
