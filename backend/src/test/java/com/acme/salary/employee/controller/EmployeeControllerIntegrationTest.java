package com.acme.salary.employee.controller;

import com.acme.salary.employee.dto.CreateEmployeeRequest;
import com.acme.salary.employee.dto.UpdateStatusRequest;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "viewer", authorities = {"ROLE_HR_VIEWER"})
    @DisplayName("GET /api/v1/employees as HR_VIEWER returns 200 OK and paginated records")
    void testGetEmployeesAsViewer() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].employeeCode").value("EMP-00001"));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ROLE_HR_MANAGER"})
    @DisplayName("POST /api/v1/employees as HR_MANAGER creates employee with 201 Created")
    void testCreateEmployeeAsManager() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "EMP-09999", "Sarah", "Connor", "sarah.connor@acme.corp",
                "+1-555-9999", 1L, 1L, 1L, LocalDate.of(2024, 6, 1)
        );

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP-09999"))
                .andExpect(jsonPath("$.data.firstName").value("Sarah"))
                .andExpect(jsonPath("$.data.departmentName").value("Engineering"));
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"ROLE_HR_VIEWER"})
    @DisplayName("POST /api/v1/employees as HR_VIEWER returns 403 Forbidden")
    void testCreateEmployeeAsViewerForbidden() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "EMP-08888", "Kyle", "Reese", "kyle@acme.corp",
                null, 1L, 1L, 1L, LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"ROLE_HR_MANAGER"})
    @DisplayName("PATCH /api/v1/employees/1/status updates employee status")
    void testUpdateEmployeeStatus() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest("ON_LEAVE");

        mockMvc.perform(patch("/api/v1/employees/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ON_LEAVE"));
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"ROLE_HR_VIEWER"})
    @DisplayName("GET /api/v1/employees with filters and safe sort mappings returns filtered results")
    void testGetEmployeesWithFiltersAndSafeSorting() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .param("departmentId", "1")
                        .param("countryId", "1")
                        .param("status", "ACTIVE")
                        .param("sortBy", "fullName")
                        .param("sortDir", "asc")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"ROLE_HR_VIEWER"})
    @DisplayName("GET /api/v1/employees with unknown sortBy safely falls back to default sorting without 500 error")
    void testGetEmployeesUnknownSortFallback() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .param("sortBy", "unknownPropertyThatDoesNotExist")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
