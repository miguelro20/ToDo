package com.example.ToDo;

import com.example.ToDo.entities.Metrics;
import com.example.ToDo.entities.ToDo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CacheTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
    private final LocalDate dueDate = LocalDate.parse("March 2, 2025", formatter);
    private final LocalDate creationDate = LocalDate.parse("January 2, 2025", formatter);

    @Test
    void testGetToDoCaching() {
        String cacheKey = "allTodos";
        String url = "http://localhost:" + port + "/api/home/todos";
        
        // First call - should cache the result
        ResponseEntity<String> firstResponse = restTemplate.getForEntity(url, String.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verify cache entry exists
        assertThat(cacheManager.getCache("todos").get(cacheKey)).isNotNull();
    }

    @Test
    void testGetMetricsCaching() {
        String cacheKey = "allMetrics";
        String url = "http://localhost:" + port + "/api/metrics";
        
        // First call - should cache the result
        ResponseEntity<Metrics> firstResponse = restTemplate.getForEntity(url, Metrics.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verify cache entry exists
        assertThat(cacheManager.getCache("metrics").get(cacheKey)).isNotNull();
    }

    @Test
    void testCacheEvictionOnUpdate() {
        // First, get the initial data and verify it's cached
        String cacheKey = "allTodos";
        String getUrl = "http://localhost:" + port + "/api/home/todos";
        restTemplate.getForEntity(getUrl, String.class);
        assertThat(cacheManager.getCache("todos").get(cacheKey)).isNotNull();

        // Create a todo to update
        ToDo newTodo = new ToDo(
                "Test Task",
                "Test Description",
                "High",
                false,
                dueDate,
                null,
                creationDate
        );
        ResponseEntity<ToDo> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/todos",
                newTodo,
                ToDo.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ToDo createdTodo = createResponse.getBody();
        assertThat(createdTodo).isNotNull();
        
        // Update a todo
        createdTodo.setName("Updated Task");
        createdTodo.setDescription("Updated Description");
        
        HttpEntity<ToDo> requestEntity = new HttpEntity<>(createdTodo);
        restTemplate.exchange(
                "http://localhost:" + port + "/api/updateToDo",
                HttpMethod.PUT,
                requestEntity,
                ToDo.class
        );

        // Verify cache was evicted
        assertThat(cacheManager.getCache("todos").get(cacheKey)).isNull();
    }

    @Test
    void testCacheEvictionOnDelete() {
        // First, get the initial data and verify it's cached
        String cacheKey = "allTodos";
        String getUrl = "http://localhost:" + port + "/api/home/todos";
        restTemplate.getForEntity(getUrl, String.class);
        assertThat(cacheManager.getCache("todos").get(cacheKey)).isNotNull();

        // Create a todo to delete
        ToDo newTodo = new ToDo(
                "Delete Me",
                "This will be deleted",
                "Low",
                false,
                dueDate,
                null,
                creationDate
        );
        ResponseEntity<ToDo> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/todos",
                newTodo,
                ToDo.class
        );
        ToDo createdTodo = createResponse.getBody();
        assertThat(createdTodo).isNotNull();
        
        // Delete the todo
        restTemplate.delete("http://localhost:" + port + "/api/delete/" + createdTodo.getId());

        // Verify cache was evicted
        assertThat(cacheManager.getCache("todos").get(cacheKey)).isNull();
    }

    @Test
    void testCacheEvictionOnStatusChange() {
        // First, get the initial data and verify it's cached
        String todoCacheKey = "allTodos";
        String metricsCacheKey = "allMetrics";
        String todoUrl = "http://localhost:" + port + "/api/home/todos";
        String metricsUrl = "http://localhost:" + port + "/api/metrics";
        
        restTemplate.getForEntity(todoUrl, String.class);
        restTemplate.getForEntity(metricsUrl, Metrics.class);
        
        assertThat(cacheManager.getCache("todos").get(todoCacheKey)).isNotNull();
        assertThat(cacheManager.getCache("metrics").get(metricsCacheKey)).isNotNull();

        // Create a todo to mark as done
        ToDo newTodo = new ToDo(
                "Mark Me Done",
                "This will be marked as done",
                "Medium",
                false,
                dueDate,
                null,
                creationDate
        );
        ResponseEntity<ToDo> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/todos",
                newTodo,
                ToDo.class
        );
        ToDo createdTodo = createResponse.getBody();
        assertThat(createdTodo).isNotNull();
        
        // Change status
        restTemplate.exchange(
                "http://localhost:" + port + "/api/done/" + createdTodo.getId(),
                HttpMethod.PUT,
                null,
                Void.class
        );

        // Verify cache was evicted
        assertThat(cacheManager.getCache("todos").get(todoCacheKey)).isNull();
        assertThat(cacheManager.getCache("metrics").get(metricsCacheKey)).isNull();
    }
} 