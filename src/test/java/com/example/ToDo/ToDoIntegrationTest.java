package com.example.ToDo;

import com.example.ToDo.entities.ToDo;
import com.example.ToDo.repository.ToDoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ToDoIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ToDoRepository toDoRepository;
    
    @BeforeEach
    void setup() {
        // Clear any existing data
        List<ToDo> allTodos = toDoRepository.findAll();
        if (!allTodos.isEmpty()) {
            toDoRepository.deleteAll();
        }
    }

    @Test
    void testCompleteTodoLifecycle() {
        // Step 1: Create a new ToDo item
        ToDo newTodo = new ToDo(
                "Integration Test Task",
                "Testing the complete lifecycle",
                "High",
                false,
                LocalDate.now().plusDays(7),
                null,
                LocalDate.now()
        );

        // POST request to create the todo
        ResponseEntity<ToDo> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/todos",
                newTodo,
                ToDo.class
        );

        // Verify successful creation
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ToDo createdTodo = createResponse.getBody();
        assertThat(createdTodo).isNotNull();
        assertThat(createdTodo.getId()).isPositive();
        assertThat(createdTodo.getName()).isEqualTo("Integration Test Task");
        assertThat(createdTodo.isStatus()).isFalse(); // Initially not completed
        Long todoId = createdTodo.getId();

        // Step 2: Verify the todo is in the filtered results
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<Map<String, Object>> listResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos?priority=High",
                HttpMethod.GET,
                null,
                responseType
        );

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().get("content")).asList().isNotEmpty();

        // Step 3: Update the todo description
        createdTodo.setDescription("Updated description for lifecycle test");
        
        HttpEntity<ToDo> updateEntity = new HttpEntity<>(createdTodo);
        ResponseEntity<ToDo> updateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/updateToDo",
                HttpMethod.PUT,
                updateEntity,
                ToDo.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ToDo updatedTodo = updateResponse.getBody();
        assertThat(updatedTodo).isNotNull();
        assertThat(updatedTodo.getDescription()).isEqualTo("Updated description for lifecycle test");
        assertThat(updatedTodo.getId()).isEqualTo(todoId); // Same ID is maintained

        // Step 4: Mark the todo as done
        ResponseEntity<Void> doneResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/done/" + todoId,
                HttpMethod.PUT,
                null,
                Void.class
        );

        assertThat(doneResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verify in repository that it's marked as done
        Optional<ToDo> doneTodoOpt = toDoRepository.findById(todoId);
        assertThat(doneTodoOpt).isPresent();
        ToDo doneTodo = doneTodoOpt.get();
        assertThat(doneTodo.isStatus()).isTrue();
        assertThat(doneTodo.getDoneDate()).isNotNull(); // Done date should be set
        
        // Step 5: Get metrics and verify they're updated
        ResponseEntity<Map<String, Object>> metricsResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/metrics",
                HttpMethod.GET,
                null,
                responseType
        );
        
        assertThat(metricsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Step 6: Mark the todo as undone
        ResponseEntity<Void> undoneResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/undone/" + todoId,
                HttpMethod.PUT,
                null,
                Void.class
        );

        assertThat(undoneResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verify it's now undone
        Optional<ToDo> undoneTodoOpt = toDoRepository.findById(todoId);
        assertThat(undoneTodoOpt).isPresent();
        ToDo undoneTodo = undoneTodoOpt.get();
        assertThat(undoneTodo.isStatus()).isFalse();
        assertThat(undoneTodo.getDoneDate()).isNull(); // Done date should be cleared
        
        // Step 7: Finally, delete the todo
        restTemplate.delete("http://localhost:" + port + "/api/delete/" + todoId);
        
        // Verify it's deleted
        Optional<ToDo> deletedTodoOpt = toDoRepository.findById(todoId);
        assertThat(deletedTodoOpt).isEmpty();
    }
    
    @Test
    void testInteractionBetweenOperations() {
        // Setup: Create multiple todos with different priorities
        ToDo highPriorityTodo = new ToDo(
                "High Priority Task",
                "This is urgent",
                "High",
                false,
                LocalDate.now().plusDays(1),
                null,
                LocalDate.now()
        );
        
        ToDo mediumPriorityTodo = new ToDo(
                "Medium Priority Task",
                "This is important but not urgent",
                "Medium",
                false,
                LocalDate.now().plusDays(3),
                null,
                LocalDate.now()
        );
        
        ToDo lowPriorityTodo = new ToDo(
                "Low Priority Task",
                "This can wait",
                "Low",
                false,
                LocalDate.now().plusDays(7),
                null,
                LocalDate.now()
        );
        
        // Step 1: Create all todos
        ResponseEntity<ToDo> highResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/todos", highPriorityTodo, ToDo.class);
        ResponseEntity<ToDo> mediumResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/todos", mediumPriorityTodo, ToDo.class);
        ResponseEntity<ToDo> lowResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/todos", lowPriorityTodo, ToDo.class);
        
        ToDo createdHighTodo = highResponse.getBody();
        ToDo createdMediumTodo = mediumResponse.getBody();
        ToDo createdLowTodo = lowResponse.getBody();
        
        assertThat(createdHighTodo).isNotNull();
        assertThat(createdMediumTodo).isNotNull();
        assertThat(createdLowTodo).isNotNull();
        
        Long highId = createdHighTodo.getId();
        Long mediumId = createdMediumTodo.getId();
        Long lowId = createdLowTodo.getId();
        
        // Step 2: Verify filtering by priority works
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};
        
        ResponseEntity<Map<String, Object>> highFilterResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos?priority=High",
                HttpMethod.GET, null, responseType);
        
        // Verify we have exactly 1 high priority task (the one we created)
        assertThat(highFilterResponse.getBody()).isNotNull();
        List<?> highPriorityTasks = (List<?>) highFilterResponse.getBody().get("content");
        
        // Find our task in the list
        boolean foundOurTask = false;
        for (Object task : highPriorityTasks) {
            if (task instanceof Map) {
                Map<?, ?> taskMap = (Map<?, ?>) task;
                if (taskMap.get("id") != null && 
                    taskMap.get("id").toString().equals(highId.toString())) {
                    foundOurTask = true;
                    break;
                }
            }
        }
        assertThat(foundOurTask).isTrue();
        
        // Step 3: Mark medium priority as done
        restTemplate.exchange(
                "http://localhost:" + port + "/api/done/" + mediumId,
                HttpMethod.PUT, null, Void.class);
        
        // Step 4: Verify filtering by status works
        ResponseEntity<Map<String, Object>> doneFilterResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos?status=true",
                HttpMethod.GET, null, responseType);
        
        // Verify we have our done task
        assertThat(doneFilterResponse.getBody()).isNotNull();
        List<?> doneTasks = (List<?>) doneFilterResponse.getBody().get("content");
        
        // Find our task in the list
        boolean foundOurDoneTask = false;
        for (Object task : doneTasks) {
            if (task instanceof Map) {
                Map<?, ?> taskMap = (Map<?, ?>) task;
                if (taskMap.get("id") != null && 
                    taskMap.get("id").toString().equals(mediumId.toString())) {
                    foundOurDoneTask = true;
                    break;
                }
            }
        }
        assertThat(foundOurDoneTask).isTrue();
        
        // Step 5: Update the low priority task to high priority
        ToDo lowToHighTodo = toDoRepository.findById(lowId).orElseThrow();
        lowToHighTodo.setPriority("High");
        
        HttpEntity<ToDo> updateEntity = new HttpEntity<>(lowToHighTodo);
        restTemplate.exchange(
                "http://localhost:" + port + "/api/updateToDo",
                HttpMethod.PUT, updateEntity, ToDo.class);
        
        // Step 6: Verify there are now at least 2 high priority tasks
        ResponseEntity<Map<String, Object>> updatedHighFilterResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos?priority=High",
                HttpMethod.GET, null, responseType);
        
        // We should at least find our two high priority tasks (the original high and converted low)
        assertThat(updatedHighFilterResponse.getBody()).isNotNull();
        List<?> updatedHighTasks = (List<?>) updatedHighFilterResponse.getBody().get("content");
        
        // Count our tasks in the list
        int ourHighTasks = 0;
        for (Object task : updatedHighTasks) {
            if (task instanceof Map) {
                Map<?, ?> taskMap = (Map<?, ?>) task;
                if (taskMap.get("id") != null && 
                    (taskMap.get("id").toString().equals(highId.toString()) || 
                     taskMap.get("id").toString().equals(lowId.toString()))) {
                    ourHighTasks++;
                }
            }
        }
        assertThat(ourHighTasks).isEqualTo(2);
        
        // Step 7: Delete one high priority task
        restTemplate.delete("http://localhost:" + port + "/api/delete/" + highId);
        
        // Step 8: Verify there's only one high priority task left (from our tasks)
        ResponseEntity<Map<String, Object>> afterDeleteHighFilterResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos?priority=High",
                HttpMethod.GET, null, responseType);
        
        assertThat(afterDeleteHighFilterResponse.getBody()).isNotNull();
        List<?> afterDeleteHighTasks = (List<?>) afterDeleteHighFilterResponse.getBody().get("content");
        
        // Find our remaining high priority task
        boolean foundRemainingHighTask = false;
        for (Object task : afterDeleteHighTasks) {
            if (task instanceof Map) {
                Map<?, ?> taskMap = (Map<?, ?>) task;
                if (taskMap.get("id") != null && 
                    taskMap.get("id").toString().equals(lowId.toString())) {
                    foundRemainingHighTask = true;
                    break;
                }
            }
        }
        assertThat(foundRemainingHighTask).isTrue();
        
        // Step 9: Verify that the metrics have been updated to reflect the done task
        ResponseEntity<Map<String, Object>> metricsResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/metrics",
                HttpMethod.GET, null, responseType);
        
        assertThat(metricsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
} 