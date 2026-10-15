package com.acme.salary.audit.controller;

import com.acme.salary.audit.service.AuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditService auditService;

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_HR_ADMIN"})
    @DisplayName("GET /api/v1/audit-logs as HR_ADMIN returns 200 OK and audit logs list")
    void testGetAuditLogsAsAdmin() throws Exception {
        // Record test audit events
        auditService.recordEvent("admin", "SALARY_RECORD", 42L, "SALARY_REVISION", Map.of("amount", 120000), "127.0.0.1");

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].actorUsername").isString());
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ROLE_HR_MANAGER"})
    @DisplayName("GET /api/v1/audit-logs as HR_MANAGER returns 403 Forbidden")
    void testGetAuditLogsAsManagerForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"ROLE_HR_VIEWER"})
    @DisplayName("GET /api/v1/audit-logs as HR_VIEWER returns 403 Forbidden")
    void testGetAuditLogsAsViewerForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
