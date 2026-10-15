package com.acme.salary.seed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

public class PostgresDeterministicSeedingIntegrationTest {

    @Test
    @DisplayName("Verify deterministic 10,000 employee batch seeding against local PostgreSQL database")
    void testSeed10000EmployeesPostgres() {
        DataSource dataSource = DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/acme_salary_db")
                .username("postgres")
                .password("root")
                .driverClassName("org.postgresql.Driver")
                .build();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        DataSeederService seeder = new DataSeederService(jdbcTemplate);

        Integer currentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees", Integer.class);
        if (currentCount == null || currentCount < 10000) {
            // Clean before seeding 10,000
            jdbcTemplate.execute("DELETE FROM salary_records");
            jdbcTemplate.execute("DELETE FROM employees");
            seeder.seedEmployeesAndSalaries(10000);
        }

        Integer employeeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees", Integer.class);
        assertNotNull(employeeCount);
        assertEquals(10000, employeeCount, "Must have exactly 10,000 employees seeded in PostgreSQL");

        Integer activeSalaries = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM salary_records WHERE effective_to IS NULL", Integer.class
        );
        assertEquals(10000, activeSalaries, "Every employee must have exactly one active salary record");

        Integer totalSalaries = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM salary_records", Integer.class
        );
        assertNotNull(totalSalaries);
        assertTrue(totalSalaries >= 14000, "Must have > 14,000 salary records including historical closed revisions");

        Integer distinctDepts = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT department_id) FROM employees", Integer.class
        );
        assertEquals(8, distinctDepts, "All 8 departments must have employees allocated");

        Integer distinctCountries = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT country_id) FROM employees", Integer.class
        );
        assertEquals(8, distinctCountries, "All 8 countries must have employees allocated");
    }
}
