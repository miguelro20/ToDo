package com.example.ToDo;

import com.example.ToDo.entities.ToDo;
import com.example.ToDo.exceptions.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ValidationAndErrorHandlingTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
    private final LocalDate dueDate = LocalDate.parse("March 2, 2025", formatter);
    private final LocalDate creationDate = LocalDate.parse("January 2, 2025", formatter);

    @Test
    void testInvalidPriorityValue() {
        ToDo invalidToDo = new ToDo(
                "Test Task",
                "Test Description",
                "INVALID_PRIORITY",  // Invalid priority value
                false,
                dueDate,
                null,
                creationDate
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/todos",
                invalidToDo,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Priority must be High, Medium, or Low");
    }

    @Test
    void testMissingRequiredFields() {
        ToDo invalidToDo = new ToDo(
                "",  // Empty name
                "",  // Empty description
                "High",
                false,
                dueDate,
                null,
                creationDate
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/todos",
                invalidToDo,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Name is required")
                                                  .contains("Description is required");
    }

    @Test
    void testInvalidDateFormat() {
        ToDo invalidToDo = new ToDo(
                "Test Task",
                "Test Description",
                "High",
                false,
                null,  // Invalid date (null)
                null,
                creationDate
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/todos",
                invalidToDo,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Due date is required");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void testInvalidPaginationParameters(int invalidValue) {
        // When testing with -1, we test page parameter validation
        // When testing with 0, we test size parameter validation
        String url;
        String expectedMessage;
        
        if (invalidValue == -1) {
            // For -1, test the page parameter (page should be ≥ 0)
            url = String.format("http://localhost:%d/api/todos?page=%d&size=10", port, invalidValue);
            expectedMessage = "Page number cannot be negative";
        } else {
            // For 0, test the size parameter (size should be ≥ 1)
            url = String.format("http://localhost:%d/api/todos?page=0&size=%d", port, invalidValue);
            expectedMessage = "Page size must not be less than one";
        }
        
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                url,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains(expectedMessage);
    }
} 