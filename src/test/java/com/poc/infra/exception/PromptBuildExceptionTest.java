package com.poc.infra.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuildExceptionTest {

    @Test
    void constructor_shouldPreserveMessageAndCause() {
        Throwable cause = new IllegalStateException("causa raiz");
        PromptBuildException ex = new PromptBuildException("erro ao montar prompt", cause);

        assertEquals("erro ao montar prompt", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void exception_isCheckedException_andNotRuntimeException() {
        PromptBuildException ex = new PromptBuildException("mensagem", null);

        assertInstanceOf(Exception.class, ex, "Deve estender Exception");
        assertFalse(RuntimeException.class.isAssignableFrom(ex.getClass()), "Não deve estender RuntimeException");
    }
}
