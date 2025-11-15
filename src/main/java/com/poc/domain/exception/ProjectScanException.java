package com.poc.domain.exception;

public class ProjectScanException extends Exception {

    public ProjectScanException(String message) {
        super(message);
    }

    public ProjectScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
