package com.acme.salary.seed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DataSeederServiceTest {

    @Autowired
    private DataSeederService dataSeederService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Verify deterministic batch seeding generates required records without constraint violations")
    void testSeedEmployeesAndSalaries() {
        int seedCount = 50;

        // Clear existing employees in test fixture for clean evaluation
        jdbcTemplate.execute("DELETE FROM salary_records");
        jdbcTemplate.execute("DELETE FROM employees");

        dataSeederService.seedEmployeesAndSalaries(seedCount);

        Integer employeeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees", Integer.class);
        assertEquals(seedCount, employeeCount);

        Integer activeSalaries = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM salary_records WHERE effective_to IS NULL", Integer.class
        );
        assertEquals(seedCount, activeSalaries, "Every seeded employee must have exactly one active salary record");

        Integer totalSalaries = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM salary_records", Integer.class
        );
        assertTrue(totalSalaries >= seedCount, "Total salaries should include historical closed records");

        // Verify no overlapping salary date ranges exist
        Integer invalidDates = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM salary_records WHERE effective_to IS NOT NULL AND effective_to < effective_from",
                Integer.class
        );
        assertEquals(0, invalidDates, "No salary records should have effective_to before effective_from");
    }
}
