package com.example.ToDo.services;

import com.example.ToDo.entities.Metrics;
import com.example.ToDo.entities.ToDo;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Service
public class ToDoServiceImplementation implements ToDoService {

    String string = "March 2, 2025";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
    LocalDate date = LocalDate.parse(string, formatter);

    String creationDateString = "January 2, 2025";
    LocalDate creationDate = LocalDate.parse(creationDateString, formatter);

    List<ToDo> list = new ArrayList<>(Arrays.asList(
            new ToDo(1, "Caminar", "Ir a caminar por una hora", "High", false, date, null, creationDate),
            new ToDo(2, "Cocinar", "Cocinar la comida", "Medium", false, date, null, LocalDate.parse("March 10, 2024", formatter)),
            new ToDo(3, "Correr", "Correr dos horas", "Medium", false, date, null, LocalDate.parse("March 15, 2024", formatter)),
            new ToDo(4, "Deporte", "Realizar un deporte", "Medium", false, date, null, LocalDate.parse("December 10, 2024", formatter)),
            new ToDo(5, "Cocinar", "Cocinar la comida", "Medium", false, date, null, LocalDate.parse("July 10, 2024", formatter)),
            new ToDo(6, "Cocinar", "Cocinar la comida", "Medium", false, date, null, LocalDate.parse("January 10, 2025", formatter)),
            new ToDo(7, "Cocinar", "Cocinar la comida", "Medium", false, date, null, LocalDate.parse("January 17, 2024", formatter)),
            new ToDo(8, "Cocinar", "Cocinar la comida", "Medium", false, date, null, LocalDate.parse("August 25, 2024", formatter)),
            new ToDo(9, "Cocinar", "Cocinar la comida", "Medium", false, date, null, LocalDate.parse("February 10, 2024", formatter)),
            new ToDo(10, "Cocinar", "Cocinar la comida", "Medium", false, date, null, LocalDate.parse("January 1, 2025", formatter)),
            new ToDo(11, "Cocinar", "Cocinar la comida", "Medium", false, date, null, LocalDate.parse("March 10, 2024", formatter))
    ));

    @Override
    public List<ToDo> getToDo() {
        return list;
    }

    @Override
    public ToDo addToDo(ToDo todo) {
        list.add(todo);
        return todo;
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

        List<ToDo> filteredList = list.stream()
                .filter(todo -> (name == null || todo.getName().toLowerCase().contains(name.toLowerCase())))
                .filter(todo -> (status == null || todo.isStatus() == status))
                .filter(todo -> (priority == null || todo.getPriority().toLowerCase().contains(priority.toLowerCase())))
                .collect(Collectors.toList());


        Map<String, Integer> priorityScale= Map.of(
                "High", 1,
                "Medium", 2,
                "Low",3
                );
        Comparator<ToDo> comparator = switch (sortBy.toLowerCase()) {
            case "priority" -> Comparator.comparing(todo -> priorityScale.getOrDefault(todo.getPriority(), Integer.MAX_VALUE));
            case "due-date" -> Comparator.comparing(ToDo::getDueDate);
            default -> Comparator.comparing(ToDo::getId);

        };
        if (sortDir.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        filteredList.sort(comparator);

        int totalItems = filteredList.size();
        int fromIndex = Math.min(page * size, totalItems);
        int toIndex = Math.min(fromIndex + size, totalItems);
        List<ToDo> paginatedList = filteredList.subList(fromIndex, toIndex);

        Map<String, Object> response = new HashMap<>();
        response.put("content", paginatedList);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", totalItems);
        response.put("totalPages", (int) Math.ceil((double) totalItems / size));
        response.put("sortBy", sortBy);
        response.put("sortDir", sortDir);
        response.put("list", list);

        return response;

    }

    @Override
    public ToDo updateToDo(ToDo todo) {
        list.forEach(e -> {
            if (e.getId() == todo.getId()) {
                e.setName(todo.getName());
                e.setPriority(todo.getPriority());
                e.setDueDate(todo.getDueDate());
            }
        });
        return todo;
    }

    @Override
    public void deleteToDo(long id) {
        list = this.list.stream().filter(e -> e.getId() != id).collect(Collectors.toList());
    }

    @Override
    public void doneToDo(long toDoId) {
        list.forEach(e -> {
            if (e.getId() == toDoId) {
                e.setStatus(true);
                e.setDoneDate(LocalDate.now());
            }
        });
    }

    @Override
    public void unDoneToDo(long toDoId) {
        list.forEach(e -> {
            if (e.getId() == toDoId) {
                e.setStatus(false);
                e.setDoneDate(null);
            }
        });
    }

    @Override
    public Metrics getMetrics() {
        AtomicLong totalCounter= new AtomicLong(0);
        AtomicLong totalCount= new AtomicLong(0);
        list.forEach(e-> {
            if(e.getDoneDate()!=null){
                totalCount.addAndGet(ChronoUnit.DAYS.between(e.getCreationDate(),e.getDoneDate()));
                totalCounter.addAndGet(1);
            }
        });
        AtomicLong highCounter= new AtomicLong(0);
        AtomicLong highCount= new AtomicLong(0);
        list.forEach(e-> {
            if(e.getDoneDate()!=null && e.getPriority().equalsIgnoreCase("High")){
                highCount.addAndGet(ChronoUnit.DAYS.between(e.getCreationDate(),e.getDoneDate()));
                highCounter.addAndGet(1);
            }
        });
        AtomicLong mediumCounter= new AtomicLong(0);
        AtomicLong mediumCount= new AtomicLong(0);
        list.forEach(e-> {
            if(e.getDoneDate()!=null && e.getPriority().equalsIgnoreCase("Medium")){
                mediumCount.addAndGet(ChronoUnit.DAYS.between(e.getCreationDate(),e.getDoneDate()));
                mediumCounter.addAndGet(1);
            }
        });
        AtomicLong lowCounter= new AtomicLong(0);
        AtomicLong lowCount= new AtomicLong(0);
        list.forEach(e-> {
            if(e.getDoneDate()!=null && e.getPriority().equalsIgnoreCase("Low")){
                lowCount.addAndGet(ChronoUnit.DAYS.between(e.getCreationDate(),e.getDoneDate()));
                lowCounter.addAndGet(1);
            }
        });
        long totalAverage= totalCounter.get() >0 ? totalCount.get()/totalCounter.get() : 0;
        long highAverage= highCounter.get() >0 ? highCount.get()/highCounter.get() : 0;
        long mediumAverage= mediumCounter.get() >0 ? mediumCount.get()/mediumCounter.get() : 0;
        long lowAverage= lowCounter.get() >0 ? lowCount.get()/lowCounter.get() : 0;
        return new Metrics(totalAverage, highAverage, mediumAverage, lowAverage);
    }
}