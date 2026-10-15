package com.acme.salary.common.exception;

import com.acme.salary.common.response.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
    }

    @Test
    @DisplayName("ResourceNotFoundException maps to HTTP 404")
    void testResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Employee not found");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Employee not found", response.getBody().message());
    }

    @Test
    @DisplayName("SalaryOverlapException maps to HTTP 409 Conflict")
    void testSalaryOverlapException() {
        SalaryOverlapException ex = new SalaryOverlapException("Effective date overlaps with existing period");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleSalaryOverlap(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("Effective date overlaps with existing period", response.getBody().message());
    }

    @Test
    @DisplayName("RateLimitExceededException maps to HTTP 429 Too Many Requests")
    void testRateLimitExceededException() {
        RateLimitExceededException ex = new RateLimitExceededException("Account temporarily locked");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleRateLimitExceeded(ex, request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Too Many Requests", response.getBody().error());
    }

    @Test
    @DisplayName("AccessDeniedException maps to HTTP 403 Forbidden")
    void testAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Forbidden", response.getBody().error());
    }

    @Test
    @DisplayName("BadCredentialsException maps to HTTP 401 Unauthorized")
    void testBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Invalid password");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unauthorized", response.getBody().error());
    }
}
