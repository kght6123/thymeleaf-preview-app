package com.example.thymeleafpreview.exception;

public class InvalidPathException extends RuntimeException {

    private final String attemptedPath;

    public InvalidPathException(String attemptedPath) {
        super("Invalid path - possible path traversal attempt: " + attemptedPath);
        this.attemptedPath = attemptedPath;
    }

    public InvalidPathException(String attemptedPath, String reason) {
        super("Invalid path: " + attemptedPath + " - " + reason);
        this.attemptedPath = attemptedPath;
    }

    public String getAttemptedPath() {
        return attemptedPath;
    }
}
