package com.example.ToDo;

import com.example.ToDo.entities.Metrics;
import com.example.ToDo.entities.ToDo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HttpRequestTest {


    String string = "March 2, 2025";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
    LocalDate date = LocalDate.parse(string, formatter);

    String creationDateString = "January 2, 2025";
    LocalDate creationDate = LocalDate.parse(creationDateString, formatter);

    ToDo postToDo =  new ToDo(15,
            "Caminar Rapido",
            "Ir a caminar por una hora rapido",
            "High",
            false,
            date,
            null,
            creationDate);
    HttpEntity<ToDo> putToDo =  new HttpEntity<>(new ToDo(1,
            "Caminar Rapido",
            "Ir a caminar por una hora rapido",
            "High",
            false,
            date,
            null,
            creationDate)) ;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetTodos() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/home/todos",
                String.class)).contains("Caminar");
    }

    @Test
    void testGetFilteredToDos() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/api/todos?name=&status=&priority=&page=0&size=10&sortBy=id&sortDir=asc",
                String.class)).contains("content", "page", "size", "totalElements", "totalPages", "sortBy", "sortDir", "list");
    }

    @Test
    void testGetMetrics() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/api/metrics",
                Metrics.class)).hasOnlyFields("totalAverage", "highAverage", "mediumAverage", "lowAverage");
    }

    @Test
    void testAddToDo() throws Exception {
        ResponseEntity<ToDo> postResponse= restTemplate.postForEntity("http://localhost:" + port + "/api/todos",postToDo, ToDo.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getBody()).isNotNull();
        assertThat(postResponse.getBody().getName()).isEqualTo(postToDo.getName());
    }

    @Test
    void testUpdateToDo() throws Exception {
        ResponseEntity<ToDo> putResponse= restTemplate.exchange("http://localhost:" + port + "/api/updateToDo",HttpMethod.PUT, putToDo, ToDo.class);
        assertThat(putResponse.getBody()).isNotNull();
        assertThat(putResponse.getBody().getName()).isEqualTo(putToDo.getBody().getName());
    }

    @Test
    void testDoneStatus() throws Exception {
        long toDoId =1;
        ResponseEntity<Void> response = restTemplate.exchange("http://localhost:" + port + "/api/done/{toDoId}", HttpMethod.PUT, null, Void.class, toDoId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testUndoneStatus() throws Exception {
        long toDoId =1;
        ResponseEntity<Void> response = restTemplate.exchange("http://localhost:" + port + "/api/undone/{toDoId}", HttpMethod.PUT, null, Void.class, toDoId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testDeleteToDo() throws Exception {
        long toDoId =1;
        ResponseEntity<Void> response = restTemplate.exchange("http://localhost:" + port + "/api/delete/{toDoId}", HttpMethod.DELETE, null, Void.class, toDoId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

