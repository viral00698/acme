package com.acme.salary.reports.util;

public final class CsvSanitizer {

    private static final String FORMULA_TRIGGERS = "=+-@\t\r";

    private CsvSanitizer() {}

    /**
     * Sanitizes CSV field content to prevent CSV Formula Injection (CWE-1236).
     * If a cell begins with =, +, -, @, tab, or carriage return, prepends a single apostrophe.
     */
    public static String sanitize(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.isEmpty()) {
            return str;
        }

        char firstChar = str.charAt(0);
        if (FORMULA_TRIGGERS.indexOf(firstChar) >= 0) {
            return "'" + str;
        }

        return str;
    }
}
