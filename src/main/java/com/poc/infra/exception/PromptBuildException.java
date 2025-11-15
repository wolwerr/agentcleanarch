package com.poc.infra.exception;

public class PromptBuildException extends Exception {

    public PromptBuildException(String message) {
        super(message);
    }

    public PromptBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
