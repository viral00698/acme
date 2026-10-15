package com.acme.salary.analytics.service;

import com.acme.salary.analytics.dto.*;
import com.acme.salary.analytics.repository.AnalyticsRepository;
import com.acme.salary.employee.repository.EmployeeRepository;
import com.acme.salary.organization.repository.CountryRepository;
import com.acme.salary.organization.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final CountryRepository countryRepository;

    public AnalyticsService(
            AnalyticsRepository analyticsRepository,
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            CountryRepository countryRepository) {
        this.analyticsRepository = analyticsRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.countryRepository = countryRepository;
    }

    @Transactional(readOnly = true)
    public DashboardOverviewDto getOverview() {
        long totalActive = employeeRepository.countByStatus("ACTIVE");
        long totalDepartments = departmentRepository.count();
        long totalCountries = countryRepository.count();

        List<Object[]> rows = analyticsRepository.getCurrencySummaries();
        List<CurrencySalarySummaryDto> currencySummaries = new ArrayList<>();

        for (Object[] r : rows) {
            String code = (String) r[0];
            String symbol = (String) r[1];
            long count = ((Number) r[2]).longValue();
            BigDecimal min = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;
            BigDecimal max = r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO;
            BigDecimal avg = r[5] != null ? BigDecimal.valueOf(((Number) r[5]).doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            currencySummaries.add(new CurrencySalarySummaryDto(code, symbol, count, min, max, avg));
        }

        return new DashboardOverviewDto(totalActive, totalDepartments, totalCountries, currencySummaries);
    }

    @Transactional(readOnly = true)
    public List<CountryAnalyticsDto> getByCountry() {
        List<Object[]> headcounts = analyticsRepository.getCountryHeadcounts();
        List<Object[]> avgSalaries = analyticsRepository.getCountryAvgSalaries();

        Map<Long, Map<String, BigDecimal>> countryAvgMap = new HashMap<>();
        for (Object[] r : avgSalaries) {
            Long countryId = ((Number) r[0]).longValue();
            String currency = (String) r[1];
            BigDecimal avg = r[2] != null ? BigDecimal.valueOf(((Number) r[2]).doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            countryAvgMap.computeIfAbsent(countryId, k -> new HashMap<>()).put(currency, avg);
        }

        List<CountryAnalyticsDto> result = new ArrayList<>();
        for (Object[] r : headcounts) {
            Long countryId = ((Number) r[0]).longValue();
            String code = (String) r[1];
            String name = (String) r[2];
            long count = ((Number) r[3]).longValue();

            Map<String, BigDecimal> avgs = countryAvgMap.getOrDefault(countryId, Collections.emptyMap());
            result.add(new CountryAnalyticsDto(countryId, code, name, count, avgs));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<DepartmentAnalyticsDto> getByDepartment() {
        List<Object[]> headcounts = analyticsRepository.getDepartmentHeadcounts();
        List<Object[]> avgSalaries = analyticsRepository.getDepartmentAvgSalaries();

        Map<Long, Map<String, BigDecimal>> deptAvgMap = new HashMap<>();
        for (Object[] r : avgSalaries) {
            Long deptId = ((Number) r[0]).longValue();
            String currency = (String) r[1];
            BigDecimal avg = r[2] != null ? BigDecimal.valueOf(((Number) r[2]).doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            deptAvgMap.computeIfAbsent(deptId, k -> new HashMap<>()).put(currency, avg);
        }

        List<DepartmentAnalyticsDto> result = new ArrayList<>();
        for (Object[] r : headcounts) {
            Long deptId = ((Number) r[0]).longValue();
            String code = (String) r[1];
            String name = (String) r[2];
            long count = ((Number) r[3]).longValue();

            Map<String, BigDecimal> avgs = deptAvgMap.getOrDefault(deptId, Collections.emptyMap());
            result.add(new DepartmentAnalyticsDto(deptId, code, name, count, avgs));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<SalaryBandDto> getSalaryDistribution(String currency) {
        String curr = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "USD";

        List<SalaryBandDto> bands = new ArrayList<>();
        // Define standard compensation tiers
        addBand(bands, "Under 50k", BigDecimal.ZERO, new BigDecimal("50000.00"), curr);
        addBand(bands, "50k - 80k", new BigDecimal("50000.00"), new BigDecimal("80000.00"), curr);
        addBand(bands, "80k - 120k", new BigDecimal("80000.00"), new BigDecimal("120000.00"), curr);
        addBand(bands, "120k - 160k", new BigDecimal("120000.00"), new BigDecimal("160000.00"), curr);
        addBand(bands, "160k+", new BigDecimal("160000.00"), null, curr);

        return bands;
    }

    private void addBand(List<SalaryBandDto> bands, String name, BigDecimal min, BigDecimal max, String currency) {
        long count = analyticsRepository.countEmployeesInSalaryBand(currency, min, max);
        bands.add(new SalaryBandDto(name, min, max, currency, count));
    }
}
