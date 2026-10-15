package com.acme.salary.salary.controller;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SalaryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "viewer", authorities = {"ROLE_HR_VIEWER"})
    @DisplayName("GET /api/v1/employees/1/salaries returns 200 OK and salary history structure")
    void testGetSalaryHistoryAsViewer() throws Exception {
        mockMvc.perform(get("/api/v1/employees/1/salaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(1));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ROLE_HR_MANAGER"})
    @DisplayName("POST /api/v1/employees/1/salaries creates initial and then revision salary record")
    void testCreateSalaryAndRevision() throws Exception {
        // Initial salary
        CreateSalaryRequest initial = new CreateSalaryRequest(
                new BigDecimal("85000.00"), "USD", "ANNUAL", LocalDate.of(2024, 1, 1)
        );

        mockMvc.perform(post("/api/v1/employees/1/salaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initial)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.amount").value(85000.00))
                .andExpect(jsonPath("$.data.currencyCode").value("USD"))
                .andExpect(jsonPath("$.data.active").value(true));

        // Subsequent revision
        CreateSalaryRequest revision = new CreateSalaryRequest(
                new BigDecimal("92000.00"), "USD", "ANNUAL", LocalDate.of(2025, 1, 1)
        );

        mockMvc.perform(post("/api/v1/employees/1/salaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revision)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.amount").value(92000.00))
                .andExpect(jsonPath("$.data.active").value(true));

        // Overlapping revision (must fail with 409)
        CreateSalaryRequest overlapping = new CreateSalaryRequest(
                new BigDecimal("95000.00"), "USD", "ANNUAL", LocalDate.of(2024, 6, 1)
        );

        mockMvc.perform(post("/api/v1/employees/1/salaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlapping)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"ROLE_HR_VIEWER"})
    @DisplayName("POST /api/v1/employees/1/salaries as HR_VIEWER returns 403 Forbidden")
    void testCreateSalaryAsViewerForbidden() throws Exception {
        CreateSalaryRequest request = new CreateSalaryRequest(
                new BigDecimal("90000.00"), "USD", "ANNUAL", LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/employees/1/salaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ROLE_HR_MANAGER"})
    @DisplayName("POST /api/v1/employees/1/salaries with negative amount returns 400 Bad Request")
    void testInvalidAmountRejected() throws Exception {
        CreateSalaryRequest request = new CreateSalaryRequest(
                new BigDecimal("-500.00"), "USD", "ANNUAL", LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/employees/1/salaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThanOrEqualTo(1))));
    }
}
