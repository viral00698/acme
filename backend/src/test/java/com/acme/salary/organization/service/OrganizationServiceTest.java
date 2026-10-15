package com.acme.salary.organization.service;

import com.acme.salary.organization.dto.CountryDto;
import com.acme.salary.organization.dto.CurrencyDto;
import com.acme.salary.organization.dto.DepartmentDto;
import com.acme.salary.organization.dto.JobTitleDto;
import com.acme.salary.organization.entity.Country;
import com.acme.salary.organization.entity.Currency;
import com.acme.salary.organization.entity.Department;
import com.acme.salary.organization.entity.JobTitle;
import com.acme.salary.organization.repository.CountryRepository;
import com.acme.salary.organization.repository.CurrencyRepository;
import com.acme.salary.organization.repository.DepartmentRepository;
import com.acme.salary.organization.repository.JobTitleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private JobTitleRepository jobTitleRepository;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(
                departmentRepository,
                countryRepository,
                currencyRepository,
                jobTitleRepository
        );
    }

    @Test
    @DisplayName("Retrieve all departments mapped to DTOs")
    void testGetAllDepartments() {
        Department dept1 = new Department("ENG", "Engineering");
        dept1.setId(1L);
        Department dept2 = new Department("HR", "Human Resources");
        dept2.setId(2L);

        when(departmentRepository.findAll()).thenReturn(List.of(dept1, dept2));

        List<DepartmentDto> result = organizationService.getAllDepartments();

        assertEquals(2, result.size());
        assertEquals("ENG", result.get(0).code());
        assertEquals("Human Resources", result.get(1).name());
        verify(departmentRepository).findAll();
    }

    @Test
    @DisplayName("Retrieve all countries mapped to DTOs")
    void testGetAllCountries() {
        Country usa = new Country("USA", "United States", "USD");
        usa.setId(1L);
        Country gbr = new Country("GBR", "United Kingdom", "GBP");
        gbr.setId(2L);

        when(countryRepository.findAll()).thenReturn(List.of(usa, gbr));

        List<CountryDto> result = organizationService.getAllCountries();

        assertEquals(2, result.size());
        assertEquals("USA", result.get(0).code());
        assertEquals("GBP", result.get(1).defaultCurrencyCode());
    }

    @Test
    @DisplayName("Retrieve all currencies mapped to DTOs")
    void testGetAllCurrencies() {
        Currency usd = new Currency("USD", "US Dollar", "$");
        Currency eur = new Currency("EUR", "Euro", "€");

        when(currencyRepository.findAll()).thenReturn(List.of(usd, eur));

        List<CurrencyDto> result = organizationService.getAllCurrencies();

        assertEquals(2, result.size());
        assertEquals("USD", result.get(0).code());
        assertEquals("€", result.get(1).symbol());
    }

    @Test
    @DisplayName("Retrieve job titles filtered by department")
    void testGetJobTitlesByDepartment() {
        Department dept = new Department("ENG", "Engineering");
        dept.setId(10L);

        JobTitle title1 = new JobTitle("Backend Engineer", dept);
        title1.setId(1L);
        JobTitle title2 = new JobTitle("DevOps Engineer", dept);
        title2.setId(2L);

        when(jobTitleRepository.findByDepartmentId(10L)).thenReturn(List.of(title1, title2));

        List<JobTitleDto> result = organizationService.getAllJobTitles(10L);

        assertEquals(2, result.size());
        assertEquals("Backend Engineer", result.get(0).title());
        assertEquals(10L, result.get(0).departmentId());
    }
}
