package com.poc.infra.adpter.web.validator;

import com.poc.infra.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewRequestValidatorTest {

    private final ReviewRequestValidator validator = new ReviewRequestValidator();

    @Test
    void validate_doesNotThrow_forValidPath() {
        assertDoesNotThrow(() -> validator.validate("C:/meus_projetos/projeto"));
    }

    @Test
    void validate_doesNotThrow_forLengthExactly1024() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1024; i++) sb.append('a');
        String path1024 = sb.toString();
        assertEquals(1024, path1024.length());
        assertDoesNotThrow(() -> validator.validate(path1024));
    }

    @Test
    void validate_throwsInvalidRequestException_forNullOrBlank() {
        assertThrows(InvalidRequestException.class, () -> validator.validate(null));
        assertThrows(InvalidRequestException.class, () -> validator.validate(""));
        assertThrows(InvalidRequestException.class, () -> validator.validate("   "));
    }

    @Test
    void validate_throwsInvalidRequestException_whenLengthExceeds1024() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1025; i++) sb.append('a');
        String path1025 = sb.toString();
        assertEquals(1025, path1025.length());
        assertThrows(InvalidRequestException.class, () -> validator.validate(path1025));
    }
}
