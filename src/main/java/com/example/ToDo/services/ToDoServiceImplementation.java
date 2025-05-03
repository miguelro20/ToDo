package com.example.ToDo.services;

import com.example.ToDo.entities.Metrics;
import com.example.ToDo.entities.ToDo;
import com.example.ToDo.repository.ToDoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ToDoServiceImplementation implements ToDoService {

    private final ToDoRepository toDoRepository;

    @Autowired
    public ToDoServiceImplementation(ToDoRepository toDoRepository) {
        this.toDoRepository = toDoRepository;
    }

    @Override
    public List<ToDo> getToDo() {
        return toDoRepository.findAll();
    }

    @Override
    public ToDo addToDo(ToDo todo) {
        return toDoRepository.save(todo);
    }

    @Override
    public Map<String, Object> getFilteredToDos(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String name,
            Boolean status,
            String priority
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<ToDo> todoPage;
        if (name != null || status != null || priority != null) {
            todoPage = toDoRepository.findByFilters(name, status, priority, pageable);
        } else {
            todoPage = toDoRepository.findAll(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("content", todoPage.getContent());
        response.put("page", todoPage.getNumber());
        response.put("size", todoPage.getSize());
        response.put("totalElements", todoPage.getTotalElements());
        response.put("totalPages", todoPage.getTotalPages());
        response.put("sortBy", sortBy);
        response.put("sortDir", sortDir);

        return response;
    }

    @Override
    public ToDo updateToDo(ToDo todo) {
        return toDoRepository.save(todo);
    }

    @Override
    public void deleteToDo(long id) {
        toDoRepository.deleteById(id);
    }

    @Override
    public void doneToDo(long toDoId) {
        toDoRepository.findById(toDoId).ifPresent(todo -> {
            todo.setStatus(true);
            todo.setDoneDate(LocalDate.now());
            toDoRepository.save(todo);
        });
    }

    @Override
    public void unDoneToDo(long toDoId) {
        toDoRepository.findById(toDoId).ifPresent(todo -> {
            todo.setStatus(false);
            todo.setDoneDate(null);
            toDoRepository.save(todo);
        });
    }

    @Override
    public Metrics getMetrics() {
        List<ToDo> todos = toDoRepository.findAll();
        AtomicLong totalCounter = new AtomicLong(0);
        AtomicLong totalCount = new AtomicLong(0);
        todos.forEach(e -> {
            if (e.getDoneDate() != null) {
                totalCount.addAndGet(ChronoUnit.DAYS.between(e.getCreationDate(), e.getDoneDate()));
                totalCounter.addAndGet(1);
            }
        });
        AtomicLong highCounter = new AtomicLong(0);
        AtomicLong highCount = new AtomicLong(0);
        todos.forEach(e -> {
            if (e.getDoneDate() != null && e.getPriority().equalsIgnoreCase("High")) {
                highCount.addAndGet(ChronoUnit.DAYS.between(e.getCreationDate(), e.getDoneDate()));
                highCounter.addAndGet(1);
            }
        });
        AtomicLong mediumCounter = new AtomicLong(0);
        AtomicLong mediumCount = new AtomicLong(0);
        todos.forEach(e -> {
            if (e.getDoneDate() != null && e.getPriority().equalsIgnoreCase("Medium")) {
                mediumCount.addAndGet(ChronoUnit.DAYS.between(e.getCreationDate(), e.getDoneDate()));
                mediumCounter.addAndGet(1);
            }
        });
        AtomicLong lowCounter = new AtomicLong(0);
        AtomicLong lowCount = new AtomicLong(0);
        todos.forEach(e -> {
            if (e.getDoneDate() != null && e.getPriority().equalsIgnoreCase("Low")) {
                lowCount.addAndGet(ChronoUnit.DAYS.between(e.getCreationDate(), e.getDoneDate()));
                lowCounter.addAndGet(1);
            }
        });
        long totalAverage = totalCounter.get() > 0 ? totalCount.get() / totalCounter.get() : 0;
        long highAverage = highCounter.get() > 0 ? highCount.get() / highCounter.get() : 0;
        long mediumAverage = mediumCounter.get() > 0 ? mediumCount.get() / mediumCounter.get() : 0;
        long lowAverage = lowCounter.get() > 0 ? lowCount.get() / lowCounter.get() : 0;
        return new Metrics(totalAverage, highAverage, mediumAverage, lowAverage);
    }
}