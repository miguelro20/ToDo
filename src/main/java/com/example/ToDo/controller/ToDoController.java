package com.example.ToDo.controller;

import com.example.ToDo.entities.Metrics;
import com.example.ToDo.entities.ToDo;
import com.example.ToDo.exceptions.ToDoException;
import com.example.ToDo.services.ToDoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080")
public class ToDoController {

    @Autowired
    private ToDoService toDoService;

    @GetMapping("/home/todos")
    public List<ToDo> getToDos() {
        return this.toDoService.getToDo();
    }

    @PostMapping("/todos")
    public ResponseEntity<ToDo> addToDo(@Valid @RequestBody ToDo todo) {
        try {
            ToDo toDoReturn = this.toDoService.addToDo(todo);
            return new ResponseEntity<>(toDoReturn, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ToDoException("TODO_CREATION_ERROR", "Failed to create todo: " + e.getMessage());
        }
    }

    @GetMapping("/metrics")
    public Metrics getMetrics() {
        return this.toDoService.getMetrics();
    }

    @GetMapping("/todos")
    public Map<String, Object> getFilteredToDos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String priority
    ) {
        return toDoService.getFilteredToDos(page, size, sortBy, sortDir, name, status, priority);
    }

    @PutMapping("/updateToDo")
    public ToDo updateToDo(@Valid @RequestBody ToDo todo) {
        try {
            return this.toDoService.updateToDo(todo);
        } catch (Exception e) {
            throw new ToDoException("TODO_UPDATE_ERROR", "Failed to update todo: " + e.getMessage());
        }
    }

    @PutMapping("/done/{toDoId}")
    public ResponseEntity<HttpStatus> doneToDo(@PathVariable long toDoId) {
        try {
            this.toDoService.doneToDo(toDoId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new ToDoException("TODO_UPDATE_ERROR", "Failed to mark todo as done: " + e.getMessage());
        }
    }

    @PutMapping("/undone/{toDoId}")
    public ResponseEntity<HttpStatus> unDoneToDo(@PathVariable long toDoId) {
        try {
            this.toDoService.unDoneToDo(toDoId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new ToDoException("TODO_UPDATE_ERROR", "Failed to mark todo as undone: " + e.getMessage());
        }
    }

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
