package com.poc.infra.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidRequestExceptionTest {

    @Test
    void constructor_shouldPreserveMessage() {
        String msg = "parâmetro inválido";
        InvalidRequestException ex = new InvalidRequestException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void exception_isRuntimeException_and_hasNoCauseByDefault() {
        InvalidRequestException ex = new InvalidRequestException("teste");
        assertInstanceOf(RuntimeException.class, ex);
        assertNull(ex.getCause());
    }
}
