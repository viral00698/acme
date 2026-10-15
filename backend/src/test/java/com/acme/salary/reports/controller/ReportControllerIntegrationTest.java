package com.acme.salary.reports.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "viewer", authorities = {"ROLE_HR_VIEWER"})
    @DisplayName("GET /api/v1/reports/salaries returns 200 OK and paginated report data")
    void testGetSalaryReport() throws Exception {
        mockMvc.perform(get("/api/v1/reports/salaries")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ROLE_HR_MANAGER"})
    @DisplayName("GET /api/v1/reports/salaries/export as HR_MANAGER streams CSV with 200 OK")
    void testExportSalaryReportCsvAsManager() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/reports/salaries/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", startsWith("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("acme_salaries_report.csv")))
                .andReturn();

        // Async dispatch for StreamingResponseBody
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Employee Code,Employee Name,Email,Department,Country,Job Title,Status,Hire Date,Salary Amount,Currency,Pay Frequency,Annualized Salary (Est),Effective From,Effective To")));
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"ROLE_HR_VIEWER"})
    @DisplayName("GET /api/v1/reports/salaries/export as HR_VIEWER returns 403 Forbidden")
    void testExportSalaryReportCsvAsViewerForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/reports/salaries/export"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
