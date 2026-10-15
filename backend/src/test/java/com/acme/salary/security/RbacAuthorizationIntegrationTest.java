package com.acme.salary.security;

import com.acme.salary.employee.dto.CreateEmployeeRequest;
import com.acme.salary.salary.dto.CreateSalaryRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RbacAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Unauthenticated request to protected endpoints must return 401 Unauthorized")
    void testUnauthenticatedAccessReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/analytics/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/reports/salaries"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"ROLE_HR_VIEWER"})
    @DisplayName("HR_VIEWER can read data but is forbidden from modifying employees or salaries")
    void testHrViewerForbiddenFromMutations() throws Exception {
        // Can read
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/reports/salaries"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/analytics/overview"))
                .andExpect(status().isOk());

        // Forbidden from creating employee
        CreateEmployeeRequest empRequest = new CreateEmployeeRequest(
                "EMP-SEC-01", "Sec", "Test", "sec@acme.corp", "555-0199",
                1L, 1L, 1L, LocalDate.of(2025, 1, 1)
        );
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empRequest)))
                .andExpect(status().isForbidden());

        // Forbidden from revising salary
        CreateSalaryRequest salaryRequest = new CreateSalaryRequest(
                new BigDecimal("99000.00"), "USD", "ANNUAL", LocalDate.of(2025, 5, 1)
        );
        mockMvc.perform(post("/api/v1/employees/1/salaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salaryRequest)))
                .andExpect(status().isForbidden());

        // Forbidden from exporting CSV
        mockMvc.perform(get("/api/v1/reports/salaries/export"))
                .andExpect(status().isForbidden());

        // Forbidden from audit logs
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ROLE_HR_MANAGER"})
    @DisplayName("HR_MANAGER can export reports but is forbidden from inspecting audit logs")
    void testHrManagerCannotAccessAuditLogs() throws Exception {
        // Manager can export CSV
        mockMvc.perform(get("/api/v1/reports/salaries/export"))
                .andExpect(status().isOk());

        // Manager is forbidden from audit logs
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_HR_ADMIN"})
    @DisplayName("HR_ADMIN has full access to audit logs and all operations")
    void testHrAdminHasAuditLogAccess() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk());
    }
}
