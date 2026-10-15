package com.acme.salary.employee.service;

import com.acme.salary.audit.service.AuditService;
import com.acme.salary.common.exception.DuplicateResourceException;
import com.acme.salary.common.exception.InvalidOperationException;
import com.acme.salary.common.exception.ResourceNotFoundException;
import com.acme.salary.common.response.PageResponse;
import com.acme.salary.employee.dto.*;
import com.acme.salary.employee.entity.Employee;
import com.acme.salary.employee.repository.EmployeeRepository;
import com.acme.salary.employee.repository.EmployeeSpecification;
import com.acme.salary.organization.entity.Country;
import com.acme.salary.organization.entity.Department;
import com.acme.salary.organization.entity.JobTitle;
import com.acme.salary.organization.repository.CountryRepository;
import com.acme.salary.organization.repository.DepartmentRepository;
import com.acme.salary.organization.repository.JobTitleRepository;
import com.acme.salary.salary.entity.SalaryRecord;
import com.acme.salary.salary.repository.SalaryRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final CountryRepository countryRepository;
    private final JobTitleRepository jobTitleRepository;
    private final SalaryRecordRepository salaryRecordRepository;
    private final AuditService auditService;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            CountryRepository countryRepository,
            JobTitleRepository jobTitleRepository,
            SalaryRecordRepository salaryRecordRepository,
            AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.countryRepository = countryRepository;
        this.jobTitleRepository = jobTitleRepository;
        this.salaryRecordRepository = salaryRecordRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeSummaryDto> searchEmployees(
            String search,
            Long departmentId,
            Long countryId,
            Long jobTitleId,
            String status,
            Pageable pageable) {

        Specification<Employee> spec = EmployeeSpecification.filterBy(
                search, departmentId, countryId, jobTitleId, status
        );

        Page<EmployeeSummaryDto> page = employeeRepository.findAll(spec, pageable)
                .map(this::mapToSummaryDto);

        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public EmployeeDetailDto getEmployee(Long id) {
        Employee employee = employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        SalaryRecord activeSalary = salaryRecordRepository.findActiveByEmployeeId(id).orElse(null);
        return mapToDetailDto(employee, activeSalary);
    }

    @Transactional
    public EmployeeDetailDto createEmployee(CreateEmployeeRequest request, String actor, String clientIp) {
        if (employeeRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new DuplicateResourceException("Employee code is already in use: " + request.employeeCode());
        }
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Employee email is already in use: " + request.email());
        }

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));
        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with id: " + request.countryId()));
        JobTitle jobTitle = jobTitleRepository.findById(request.jobTitleId())
                .orElseThrow(() -> new ResourceNotFoundException("Job title not found with id: " + request.jobTitleId()));

        Employee employee = new Employee(
                request.employeeCode(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber(),
                department,
                country,
                jobTitle,
                "ACTIVE",
                request.hireDate()
        );

        Employee saved = employeeRepository.save(employee);

        auditService.recordEvent(
                actor,
                "EMPLOYEE",
                saved.getId(),
                "CREATE",
                Map.of(
                        "employeeCode", saved.getEmployeeCode(),
                        "email", saved.getEmail(),
                        "department", department.getName(),
                        "country", country.getName()
                ),
                clientIp
        );

        return mapToDetailDto(saved, null);
    }

    @Transactional
    public EmployeeDetailDto updateEmployee(Long id, UpdateEmployeeRequest request, String actor, String clientIp) {
        Employee employee = employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (!employee.getEmail().equalsIgnoreCase(request.email()) && employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already in use: " + request.email());
        }

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));
        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with id: " + request.countryId()));
        JobTitle jobTitle = jobTitleRepository.findById(request.jobTitleId())
                .orElseThrow(() -> new ResourceNotFoundException("Job title not found with id: " + request.jobTitleId()));

        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhoneNumber(request.phoneNumber());
        employee.setDepartment(department);
        employee.setCountry(country);
        employee.setJobTitle(jobTitle);
        employee.setHireDate(request.hireDate());

        Employee updated = employeeRepository.save(employee);

        auditService.recordEvent(
                actor,
                "EMPLOYEE",
                updated.getId(),
                "UPDATE",
                Map.of("email", updated.getEmail(), "department", department.getName()),
                clientIp
        );

        SalaryRecord activeSalary = salaryRecordRepository.findActiveByEmployeeId(id).orElse(null);
        return mapToDetailDto(updated, activeSalary);
    }

    @Transactional
    public EmployeeDetailDto updateStatus(Long id, UpdateStatusRequest request, String actor, String clientIp) {
        Employee employee = employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        String oldStatus = employee.getStatus();
        employee.setStatus(request.status().toUpperCase());
        Employee updated = employeeRepository.save(employee);

        auditService.recordEvent(
                actor,
                "EMPLOYEE",
                updated.getId(),
                "STATUS_CHANGE",
                Map.of("oldStatus", oldStatus, "newStatus", updated.getStatus()),
                clientIp
        );

        SalaryRecord activeSalary = salaryRecordRepository.findActiveByEmployeeId(id).orElse(null);
        return mapToDetailDto(updated, activeSalary);
    }

    @Transactional
    public void deleteEmployee(Long id, String actor, String clientIp) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (salaryRecordRepository.countByEmployeeId(id) > 0) {
            throw new InvalidOperationException(
                    "Cannot physically delete employee with existing compensation records. " +
                    "Please set the employee status to INACTIVE or TERMINATED instead."
            );
        }

        employeeRepository.delete(employee);
        auditService.recordEvent(actor, "EMPLOYEE", id, "DELETE", Map.of("code", employee.getEmployeeCode()), clientIp);
    }

    private EmployeeSummaryDto mapToSummaryDto(Employee e) {
        return new EmployeeSummaryDto(
                e.getId(),
                e.getEmployeeCode(),
                e.getFirstName(),
                e.getLastName(),
                e.getEmail(),
                e.getPhoneNumber(),
                e.getDepartment().getId(),
                e.getDepartment().getName(),
                e.getCountry().getId(),
                e.getCountry().getName(),
                e.getCountry().getCode(),
                e.getJobTitle().getId(),
                e.getJobTitle().getTitle(),
                e.getStatus(),
                e.getHireDate()
        );
    }

    private EmployeeDetailDto mapToDetailDto(Employee e, SalaryRecord salary) {
        return new EmployeeDetailDto(
                e.getId(),
                e.getEmployeeCode(),
                e.getFirstName(),
                e.getLastName(),
                e.getEmail(),
                e.getPhoneNumber(),
                e.getDepartment().getId(),
                e.getDepartment().getName(),
                e.getCountry().getId(),
                e.getCountry().getName(),
                e.getCountry().getCode(),
                e.getJobTitle().getId(),
                e.getJobTitle().getTitle(),
                e.getStatus(),
                e.getHireDate(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                salary != null ? salary.getAmount() : null,
                salary != null ? salary.getCurrency().getCode() : null,
                salary != null ? salary.getCurrency().getSymbol() : null,
                salary != null ? salary.getPayFrequency() : null,
                salary != null ? salary.getEffectiveFrom() : null
        );
    }
}
