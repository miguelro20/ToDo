package com.example.ToDo.config;

import com.example.ToDo.entities.ToDo;
import com.example.ToDo.repository.ToDoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ToDoRepository toDoRepository;

    @Autowired
    public DataInitializer(ToDoRepository toDoRepository) {
        this.toDoRepository = toDoRepository;
    }

    @Override
    public void run(String... args) {
        if (toDoRepository.count() == 0) {
            String string = "March 2, 2025";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
            LocalDate date = LocalDate.parse(string, formatter);

            toDoRepository.saveAll(Arrays.asList(
                    new ToDo("Walk", "Walk for an hour", "High", false, date, null, LocalDate.parse("January 2, 2025", formatter)),
                    new ToDo("Cook", "Cook 3 meals in a day", "Medium", false, date, null, LocalDate.parse("March 10, 2024", formatter)),
                    new ToDo("Run", "Run for two hours", "Low", false, date, null, LocalDate.parse("March 15, 2024", formatter)),
                    new ToDo("Sports", "Start doing a sport", "Medium", false, date, null, LocalDate.parse("December 10, 2024", formatter)),
                    new ToDo("Movies", "Watch a movie", "Medium", false, date, null, LocalDate.parse("July 10, 2024", formatter)),
                    new ToDo("Go Out", "Go out to dinner once", "Medium", false, date, null, LocalDate.parse("January 10, 2025", formatter)),
                    new ToDo("Write", "Write your weekly essay", "High", false, date, null, LocalDate.parse("January 17, 2024", formatter)),
                    new ToDo("Teach", "Start tutoring others", "Medium", false, date, null, LocalDate.parse("August 25, 2024", formatter)),
                    new ToDo("TV", "Buy a new tv", "Medium", false, date, null, LocalDate.parse("February 10, 2024", formatter)),
                    new ToDo("Shoes", "Buy new shoes", "Low", false, date, null, LocalDate.parse("January 1, 2025", formatter)),
                    new ToDo("Dog", "Take the dog for a walk", "Medium", false, date, null, LocalDate.parse("March 10, 2024", formatter))
            ));
        }
    }
} 