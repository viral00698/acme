package com.acme.salary.analytics.service;

import com.acme.salary.analytics.dto.DashboardOverviewDto;
import com.acme.salary.analytics.dto.SalaryBandDto;
import com.acme.salary.analytics.repository.AnalyticsRepository;
import com.acme.salary.employee.repository.EmployeeRepository;
import com.acme.salary.organization.repository.CountryRepository;
import com.acme.salary.organization.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private CountryRepository countryRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(
                analyticsRepository,
                employeeRepository,
                departmentRepository,
                countryRepository
        );
    }

    @Test
    @DisplayName("Compute dashboard overview metrics strictly grouped by currency")
    void testGetOverview() {
        when(employeeRepository.countByStatus("ACTIVE")).thenReturn(9500L);
        when(departmentRepository.count()).thenReturn(8L);
        when(countryRepository.count()).thenReturn(6L);

        Object[] usdRow = new Object[]{"USD", "$", 4500L, new BigDecimal("50000.00"), new BigDecimal("220000.00"), 112000.50};
        Object[] eurRow = new Object[]{"EUR", "€", 3000L, new BigDecimal("42000.00"), new BigDecimal("180000.00"), 89000.25};
        when(analyticsRepository.getCurrencySummaries()).thenReturn(List.of(usdRow, eurRow));

        DashboardOverviewDto overview = analyticsService.getOverview();

        assertNotNull(overview);
        assertEquals(9500L, overview.totalActiveEmployees());
        assertEquals(8L, overview.totalDepartments());
        assertEquals(6L, overview.totalCountries());
        assertEquals(2, overview.currencySummaries().size());

        assertEquals("USD", overview.currencySummaries().get(0).currencyCode());
        assertEquals(4500L, overview.currencySummaries().get(0).employeeCount());
        assertEquals(new BigDecimal("50000.00"), overview.currencySummaries().get(0).minSalary());
    }

    @Test
    @DisplayName("Salary distribution partitions into 5 distinct compensation bands")
    void testGetSalaryDistribution() {
        when(analyticsRepository.countEmployeesInSalaryBand(eq("USD"), any(), any())).thenReturn(120L);

        List<SalaryBandDto> bands = analyticsService.getSalaryDistribution("USD");

        assertNotNull(bands);
        assertEquals(5, bands.size());
        assertEquals("Under 50k", bands.get(0).bandName());
        assertEquals("50k - 80k", bands.get(1).bandName());
        assertEquals("80k - 120k", bands.get(2).bandName());
        assertEquals("120k - 160k", bands.get(3).bandName());
        assertEquals("160k+", bands.get(4).bandName());
    }
}
