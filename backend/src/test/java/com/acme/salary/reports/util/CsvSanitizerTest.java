package com.acme.salary.reports.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvSanitizerTest {

    @ParameterizedTest
    @ValueSource(strings = {"=cmd|' /C calc'!A0", "+2+5", "-100", "@SUM(A1:A10)", "\tmalicious", "\rline"})
    @DisplayName("Prepend single quote to CSV formula injection trigger characters")
    void testSanitizeFormulaTriggers(String maliciousInput) {
        String sanitized = CsvSanitizer.sanitize(maliciousInput);
        assertEquals("'" + maliciousInput, sanitized);
    }

    @Test
    @DisplayName("Leave safe text unchanged")
    void testSafeTextUnchanged() {
        assertEquals("Engineering", CsvSanitizer.sanitize("Engineering"));
        assertEquals("EMP-00100", CsvSanitizer.sanitize("EMP-00100"));
        assertEquals("John Doe", CsvSanitizer.sanitize("John Doe"));
        assertEquals("125000.00", CsvSanitizer.sanitize("125000.00"));
        assertEquals("", CsvSanitizer.sanitize(""));
        assertEquals("", CsvSanitizer.sanitize(null));
    }
}
