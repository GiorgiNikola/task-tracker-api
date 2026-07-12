package com.giorgi.tasktrackerapi.exception;

public class ProjectHasTasksException extends RuntimeException {
    public ProjectHasTasksException(String message) {
        super(message);
    }
}
