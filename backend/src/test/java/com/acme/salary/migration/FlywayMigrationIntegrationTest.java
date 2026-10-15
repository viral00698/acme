package com.acme.salary.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayMigrationIntegrationTest {

    @Test
    @DisplayName("Verify Flyway migrations execute successfully on empty PostgreSQL database")
    void testFlywayMigration() {
        DataSource dataSource = DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/acme_salary_db")
                .username("postgres")
                .password("root")
                .driverClassName("org.postgresql.Driver")
                .build();

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
        assertTrue(flyway.info().applied().length >= 3, "Expected at least 3 migrations to be applied");
    }
}
