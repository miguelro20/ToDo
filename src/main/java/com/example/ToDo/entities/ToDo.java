package com.example.ToDo.entities;


import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.Date;

public class ToDo {
    private long id;
    private String name;
    private String description;
    private String priority;
    private Boolean status;
    private LocalDate dueDate;
    private LocalDate doneDate;
    private LocalDate creationDate;

    public ToDo(long id, String name, String description, String priority, Boolean status, LocalDate dueDate, LocalDate doneDate, LocalDate creationDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.status= status;
        this.dueDate= dueDate;
        this.doneDate= doneDate;
        this.creationDate= creationDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Boolean isStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status= status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getDoneDate() {
        return doneDate;
    }

    public void setDoneDate(LocalDate doneDate) {
        this.doneDate = doneDate;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }
}
