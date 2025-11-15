package com.poc.domain.exception;

public class RepositoryPreparationException extends Exception {

    public RepositoryPreparationException(String message) {
        super(message);
    }

    public RepositoryPreparationException(String message, Throwable cause) {
        super(message, cause);
    }
}
