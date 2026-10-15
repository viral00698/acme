package com.acme.salary.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DataSeederRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederRunner.class);

    private final DataSeederService dataSeederService;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.employee-count:10000}")
    private int employeeCount;

    public DataSeederRunner(DataSeederService dataSeederService, JdbcTemplate jdbcTemplate) {
        this.dataSeederService = dataSeederService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Data seeding is disabled via configuration (app.seed.enabled=false).");
            return;
        }

        Integer currentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees", Integer.class);
        if (currentCount != null && currentCount >= employeeCount) {
            log.info("Employee directory already populated with {} records (target: {}). Skipping seed.",
                    currentCount, employeeCount);
            return;
        }

        log.info("Current employee count is {} (target: {}). Initiating deterministic seed...",
                currentCount != null ? currentCount : 0, employeeCount);

        try {
            dataSeederService.seedEmployeesAndSalaries(employeeCount);
            log.info("Deterministic database seeding complete! 10,000 employees ready for assessment.");
        } catch (Exception e) {
            log.error("Error during deterministic data seeding: {}", e.getMessage(), e);
        }
    }
}
