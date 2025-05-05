package com.example.ToDo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCorsForAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/todos")
                .header(HttpHeaders.ORIGIN, "http://localhost:8080")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8080"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,OPTIONS"))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
    }

    @Test
    void testCorsForDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/todos")
                .header(HttpHeaders.ORIGIN, "http://malicious-site.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testApiEndpointsAreAccessible() throws Exception {
        // Test that API endpoints are accessible without authentication
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    void testActuatorEndpointsRequireAuthentication() throws Exception {
        // Test that actuator endpoints require authentication
        // Spring Security returns 403 Forbidden for unauthenticated requests to protected endpoints
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCsrfProtectionIsDisabled() throws Exception {
        // Test that CSRF protection is disabled for API endpoints
        // This allows POST requests without a CSRF token
        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Task\",\"description\":\"Test Description\",\"priority\":\"High\",\"status\":false,\"dueDate\":\"2025-01-01\",\"creationDate\":\"2025-01-01\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void testAllowedHttpMethods() throws Exception {
        // Verify that all required HTTP methods are allowed
        mockMvc.perform(options("/api/todos")
                .header(HttpHeaders.ORIGIN, "http://localhost:8080")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk());

        mockMvc.perform(options("/api/todos")
                .header(HttpHeaders.ORIGIN, "http://localhost:8080")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk());

        mockMvc.perform(options("/api/todos")
                .header(HttpHeaders.ORIGIN, "http://localhost:8080")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT"))
                .andExpect(status().isOk());

        mockMvc.perform(options("/api/todos")
                .header(HttpHeaders.ORIGIN, "http://localhost:8080")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE"))
                .andExpect(status().isOk());
    }
} 