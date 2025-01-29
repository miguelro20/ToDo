package com.example.ToDo.services;

import com.example.ToDo.entities.Metrics;
import com.example.ToDo.entities.ToDo;

import java.util.List;
import java.util.Map;

public interface ToDoService {
    public List<ToDo>getToDo();

    public ToDo addToDo(ToDo todo);

    Map<String, Object> getFilteredToDos (
            int page,
            int size,
            String sortBy,
            String sortDir,
            String name,
            Boolean status,
            String priority
    );

    public ToDo updateToDo(ToDo todo);

    public void deleteToDo(long toDoId);

    public void doneToDo(long toDoId);

    public void unDoneToDo(long toDoId);

    public Metrics getMetrics();
}
