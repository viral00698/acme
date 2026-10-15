package com.acme.salary.organization.service;

import com.acme.salary.organization.dto.*;
import com.acme.salary.organization.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final DepartmentRepository departmentRepository;
    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;
    private final JobTitleRepository jobTitleRepository;

    public OrganizationService(
            DepartmentRepository departmentRepository,
            CountryRepository countryRepository,
            CurrencyRepository currencyRepository,
            JobTitleRepository jobTitleRepository) {
        this.departmentRepository = departmentRepository;
        this.countryRepository = countryRepository;
        this.currencyRepository = currencyRepository;
        this.jobTitleRepository = jobTitleRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(d -> new DepartmentDto(d.getId(), d.getCode(), d.getName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CountryDto> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(c -> new CountryDto(c.getId(), c.getCode(), c.getName(), c.getDefaultCurrencyCode()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CurrencyDto> getAllCurrencies() {
        return currencyRepository.findAll().stream()
                .map(c -> new CurrencyDto(c.getCode(), c.getName(), c.getSymbol()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobTitleDto> getAllJobTitles(Long departmentId) {
        var titles = departmentId != null
                ? jobTitleRepository.findByDepartmentId(departmentId)
                : jobTitleRepository.findAll();

        return titles.stream()
                .map(j -> new JobTitleDto(
                        j.getId(),
                        j.getTitle(),
                        j.getDepartment().getId(),
                        j.getDepartment().getName()
                ))
                .collect(Collectors.toList());
    }
}
