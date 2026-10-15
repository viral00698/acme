package com.acme.salary.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class DataSeederService {

    private static final Logger log = LoggerFactory.getLogger(DataSeederService.class);

    private final JdbcTemplate jdbcTemplate;

    private static final String[] FIRST_NAMES = {
            "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda",
            "David", "Elizabeth", "William", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
            "Thomas", "Sarah", "Charles", "Karen", "Christopher", "Nancy", "Daniel", "Lisa",
            "Matthew", "Betty", "Anthony", "Margaret", "Mark", "Sandra", "Donald", "Ashley",
            "Steven", "Kimberly", "Paul", "Emily", "Andrew", "Donna", "Joshua", "Michelle",
            "Kenneth", "Carol", "Kevin", "Amanda", "Brian", "Melissa", "George", "Deborah",
            "Timothy", "Stephanie"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
            "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White",
            "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker", "Young",
            "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
            "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell",
            "Carter", "Roberts"
    };

    private static final Map<Long, String> COUNTRY_CURRENCY_MAP = Map.of(
            1L, "USD", // USA
            2L, "GBP", // GBR
            3L, "EUR", // DEU
            4L, "INR", // IND
            5L, "JPY", // JPN
            6L, "CAD", // CAN
            7L, "AUD", // AUS
            8L, "SGD"  // SGP
    );

    private static final Map<String, Double> CURRENCY_MULTIPLIER = Map.of(
            "USD", 1.0,
            "GBP", 0.78,
            "EUR", 0.92,
            "INR", 28.0,
            "JPY", 125.0,
            "CAD", 1.34,
            "AUD", 1.48,
            "SGD", 1.35
    );

    public DataSeederService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void seedEmployeesAndSalaries(int targetCount) {
        log.info("Starting deterministic seeding of {} synthetic employees and compensation records...", targetCount);
        long startTime = System.currentTimeMillis();

        // 1. Fetch available Job Titles grouped by department
        Map<Long, List<Long>> deptJobTitles = new HashMap<>();
        jdbcTemplate.query("SELECT id, department_id FROM job_titles", rs -> {
            long titleId = rs.getLong("id");
            long deptId = rs.getLong("department_id");
            deptJobTitles.computeIfAbsent(deptId, k -> new ArrayList<>()).add(titleId);
        });

        if (deptJobTitles.isEmpty()) {
            throw new IllegalStateException("Reference data missing: job_titles not found. Ensure migrations or seed scripts executed first.");
        }

        List<Long> deptIds = new ArrayList<>(deptJobTitles.keySet());

        List<Long> countryIds = new ArrayList<>();
        Map<Long, String> countryCurrencyMap = new HashMap<>();
        jdbcTemplate.query("SELECT id, default_currency_code FROM countries", rs -> {
            long cid = rs.getLong("id");
            countryIds.add(cid);
            countryCurrencyMap.put(cid, rs.getString("default_currency_code"));
        });

        if (countryIds.isEmpty()) {
            throw new IllegalStateException("Reference data missing: countries not found.");
        }

        // Fixed seed ensures 100% deterministic reproducibility across environments
        Random random = new Random(42L);

        List<Object[]> employeeBatch = new ArrayList<>(1000);
        List<Object[]> salaryBatch = new ArrayList<>(1500);

        String employeeInsertSql = "INSERT INTO employees " +
                "(id, employee_code, first_name, last_name, email, phone_number, department_id, country_id, job_title_id, status, hire_date, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String salaryInsertSql = "INSERT INTO salary_records " +
                "(employee_id, amount, currency_code, pay_frequency, effective_from, effective_to, created_by_id, created_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Timestamp now = Timestamp.from(Instant.now());

        for (int i = 1; i <= targetCount; i++) {
            long empId = i;
            String employeeCode = String.format("EMP-%05d", i);

            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String email = String.format("%s.%s.%05d@acme.corp", firstName.toLowerCase(), lastName.toLowerCase(), i);
            String phone = String.format("+1-555-%04d", 1000 + (i % 9000));

            // Select valid department and country from database
            long deptId = deptIds.get(i % deptIds.size());
            long countryId = countryIds.get((i * 3) % countryIds.size());

            List<Long> availableTitles = deptJobTitles.get(deptId);
            long jobTitleId = availableTitles != null && !availableTitles.isEmpty()
                    ? availableTitles.get(random.nextInt(availableTitles.size()))
                    : 1L;

            // Status distribution: ~92% ACTIVE, ~4% INACTIVE, ~3% ON_LEAVE, ~1% TERMINATED
            int statusRoll = i % 100;
            String status;
            if (statusRoll < 92) {
                status = "ACTIVE";
            } else if (statusRoll < 96) {
                status = "INACTIVE";
            } else if (statusRoll < 99) {
                status = "ON_LEAVE";
            } else {
                status = "TERMINATED";
            }

            // Hire date between 2020-01-01 and 2024-12-31
            int daysOffset = random.nextInt(1800);
            LocalDate hireDate = LocalDate.of(2020, 1, 1).plusDays(daysOffset);

            employeeBatch.add(new Object[]{
                    empId, employeeCode, firstName, lastName, email, phone,
                    deptId, countryId, jobTitleId, status, Date.valueOf(hireDate), now, now
            });

            // Compensation Calculation
            String currencyCode = countryCurrencyMap.getOrDefault(countryId, "USD");
            double multiplier = CURRENCY_MULTIPLIER.getOrDefault(currencyCode, 1.0);

            // Base salary range between $55,000 and $175,000 USD
            double baseUsd = 55000.0 + (random.nextInt(120) * 1000.0);
            BigDecimal currentSalaryAmount = BigDecimal.valueOf(baseUsd * multiplier)
                    .setScale(2, RoundingMode.HALF_UP);

            // Pay Frequency: 85% ANNUAL, 10% MONTHLY, 5% BIWEEKLY
            int freqRoll = random.nextInt(100);
            String frequency = freqRoll < 85 ? "ANNUAL" : (freqRoll < 95 ? "MONTHLY" : "BIWEEKLY");

            if ("MONTHLY".equals(frequency)) {
                currentSalaryAmount = currentSalaryAmount.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            } else if ("BIWEEKLY".equals(frequency)) {
                currentSalaryAmount = currentSalaryAmount.divide(BigDecimal.valueOf(26), 2, RoundingMode.HALF_UP);
            }

            // Salary History Generation:
            // Employees hired before 2023 (~60%) get a historical closed record and a subsequent active revision
            if (hireDate.isBefore(LocalDate.of(2023, 1, 1))) {
                LocalDate revisionDate = hireDate.plusDays(365 + random.nextInt(180));
                LocalDate historicalEffectiveTo = revisionDate.minusDays(1);

                BigDecimal historicalAmount = currentSalaryAmount.multiply(BigDecimal.valueOf(0.88))
                        .setScale(2, RoundingMode.HALF_UP);

                // Closed historical record
                salaryBatch.add(new Object[]{
                        empId, historicalAmount, currencyCode, frequency,
                        Date.valueOf(hireDate), Date.valueOf(historicalEffectiveTo),
                        1L, now, 0L
                });

                // Current active record
                salaryBatch.add(new Object[]{
                        empId, currentSalaryAmount, currencyCode, frequency,
                        Date.valueOf(revisionDate), null,
                        2L, now, 1L
                });
            } else {
                // Single current active record
                salaryBatch.add(new Object[]{
                        empId, currentSalaryAmount, currencyCode, frequency,
                        Date.valueOf(hireDate), null,
                        1L, now, 0L
                });
            }

            // Flush employees in 1,000-item chunks
            if (employeeBatch.size() >= 1000) {
                jdbcTemplate.batchUpdate(employeeInsertSql, employeeBatch);
                employeeBatch.clear();
            }
        }

        // Flush remaining employees
        if (!employeeBatch.isEmpty()) {
            jdbcTemplate.batchUpdate(employeeInsertSql, employeeBatch);
            employeeBatch.clear();
        }

        // Batch insert all salary records in 1,000-item chunks
        List<Object[]> salaryChunk = new ArrayList<>(1000);
        for (Object[] sal : salaryBatch) {
            salaryChunk.add(sal);
            if (salaryChunk.size() >= 1000) {
                jdbcTemplate.batchUpdate(salaryInsertSql, salaryChunk);
                salaryChunk.clear();
            }
        }
        if (!salaryChunk.isEmpty()) {
            jdbcTemplate.batchUpdate(salaryInsertSql, salaryChunk);
            salaryChunk.clear();
        }
        salaryBatch.clear();

        // Reset PostgreSQL ID sequences to match inserted primary keys
        try {
            jdbcTemplate.execute("SELECT setval('employees_id_seq', COALESCE((SELECT MAX(id) FROM employees), 1))");
            jdbcTemplate.execute("SELECT setval('salary_records_id_seq', COALESCE((SELECT MAX(id) FROM salary_records), 1))");
        } catch (Exception e) {
            log.debug("Database sequence setval skipped or not supported on this dialect: {}", e.getMessage());
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Successfully seeded {} employees and their salary records in {} ms.", targetCount, duration);
    }
}
