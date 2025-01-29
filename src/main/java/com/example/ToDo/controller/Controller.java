package com.example.ToDo.controller;

import com.example.ToDo.entities.Metrics;
import com.example.ToDo.entities.ToDo;
import com.example.ToDo.services.ToDoService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class Controller {



    @Autowired
    public ToDoService toDoService;

    @GetMapping("/home")
    public String home() {

        return "This is home page";
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @GetMapping("home/todos")
        public List<ToDo> getToDos() {
            return this.toDoService.getToDo();
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @PostMapping("api/todos")
    public ResponseEntity<ToDo>addToDo(@RequestBody ToDo todo){
        ToDo toDoReturn = this.toDoService.addToDo(todo);
        return new ResponseEntity<>(toDoReturn,HttpStatus.CREATED);
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @GetMapping("api/metrics")
    public Metrics getMetrics() {
        return this.toDoService.getMetrics();
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @GetMapping("api/todos")
    public Map<String, Object> getFilteredToDos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String priority
    ){
        return toDoService.getFilteredToDos(page, size, sortBy, sortDir, name, status, priority);
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @PutMapping("api/updateToDo")
    public ToDo updateToDo(@RequestBody ToDo todo){
        return this.toDoService.updateToDo(todo);
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @PutMapping("api/done/{toDoId}")
    public ResponseEntity<HttpStatus> doneToDo(@PathVariable long toDoId){
        try {
            this.toDoService.doneToDo(toDoId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @PutMapping("api/undone/{toDoId}")
    public ResponseEntity<HttpStatus> unDoneToDo(@PathVariable long toDoId){
        try {
            this.toDoService.unDoneToDo(toDoId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @DeleteMapping("api/delete/{toDoId}")
    public ResponseEntity<HttpStatus> deleteToDo(@PathVariable long toDoId) {
        try {
            this.toDoService.deleteToDo(toDoId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
