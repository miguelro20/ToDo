package com.example.ToDo.exceptions;

public class ToDoException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public ToDoException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
} 