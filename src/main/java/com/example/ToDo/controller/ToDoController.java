package com.example.ToDo.controller;

import com.example.ToDo.entities.Metrics;
import com.example.ToDo.entities.ToDo;
import com.example.ToDo.exceptions.ToDoException;
import com.example.ToDo.services.ToDoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing ToDo items.
 * Provides endpoints for CRUD operations and metrics retrieval.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080")
@Validated
public class ToDoController {

    private final ToDoService toDoService;

    @Autowired
    public ToDoController(ToDoService toDoService) {
        this.toDoService = toDoService;
    }

    /**
     * Retrieves all ToDo items.
     *
     * @return List of all ToDo items
     */
    @GetMapping("/home/todos")
    public List<ToDo> getToDos() {
        return this.toDoService.getToDo();
    }

    /**
     * Creates a new ToDo item.
     *
     * @param todo The ToDo item to create
     * @return ResponseEntity containing the created ToDo item and HTTP status
     * @throws ToDoException if creation fails
     */
    @PostMapping("/todos")
    public ResponseEntity<ToDo> addToDo(@Valid @RequestBody ToDo todo) {
        try {
            ToDo toDoReturn = this.toDoService.addToDo(todo);
            return new ResponseEntity<>(toDoReturn, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ToDoException("TODO_CREATION_ERROR", "Failed to create todo: " + e.getMessage());
        }
    }

    /**
     * Retrieves metrics about ToDo items.
     *
     * @return Metrics object containing statistics
     */
    @GetMapping("/metrics")
    public Metrics getMetrics() {
        return this.toDoService.getMetrics();
    }

    /**
     * Retrieves filtered and paginated ToDo items.
     *
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc/desc)
     * @param name Filter by name (optional)
     * @param status Filter by status (optional)
     * @param priority Filter by priority (optional)
     * @return Map containing paginated results and metadata
     */
    @GetMapping("/todos")
    public Map<String, Object> getFilteredToDos(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number cannot be negative") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must not be less than one") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String priority
    ) {
        validatePaginationParameters(page, size);
        return toDoService.getFilteredToDos(page, size, sortBy, sortDir, name, status, priority);
    }

    private void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new ToDoException("INVALID_PAGE", "Page number cannot be negative");
        }
        if (size < 1) {
            throw new ToDoException("INVALID_SIZE", "Page size must not be less than one");
        }
    }

    /**
     * Updates an existing ToDo item.
     *
     * @param todo The ToDo item to update
     * @return Updated ToDo item
     * @throws ToDoException if update fails
     */
    @PutMapping("/updateToDo")
    public ToDo updateToDo(@Valid @RequestBody ToDo todo) {
        try {
            return this.toDoService.updateToDo(todo);
        } catch (Exception e) {
            throw new ToDoException("TODO_UPDATE_ERROR", "Failed to update todo: " + e.getMessage());
        }
    }

    /**
     * Marks a ToDo item as done.
     *
     * @param toDoId ID of the ToDo item to mark as done
     * @return ResponseEntity with HTTP status
     * @throws ToDoException if operation fails
     */
    @PutMapping("/done/{toDoId}")
    public ResponseEntity<HttpStatus> doneToDo(@PathVariable long toDoId) {
        try {
            this.toDoService.doneToDo(toDoId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new ToDoException("TODO_UPDATE_ERROR", "Failed to mark todo as done: " + e.getMessage());
        }
    }

    /**
     * Marks a ToDo item as undone.
     *
     * @param toDoId ID of the ToDo item to mark as undone
     * @return ResponseEntity with HTTP status
     * @throws ToDoException if operation fails
     */
    @PutMapping("/undone/{toDoId}")
    public ResponseEntity<HttpStatus> unDoneToDo(@PathVariable long toDoId) {
        try {
            this.toDoService.unDoneToDo(toDoId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new ToDoException("TODO_UPDATE_ERROR", "Failed to mark todo as undone: " + e.getMessage());
        }
    }

    /**
     * Deletes a ToDo item.
     *
     * @param toDoId ID of the ToDo item to delete
     * @return ResponseEntity with HTTP status
     * @throws ToDoException if deletion fails
     */
    @DeleteMapping("/delete/{toDoId}")
    public ResponseEntity<HttpStatus> deleteToDo(@PathVariable long toDoId) {
        try {
            this.toDoService.deleteToDo(toDoId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new ToDoException("TODO_DELETE_ERROR", "Failed to delete todo: " + e.getMessage());
        }
    }
}
