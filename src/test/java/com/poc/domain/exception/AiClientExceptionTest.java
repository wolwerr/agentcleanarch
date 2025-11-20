package com.poc.domain.exception;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class AiClientExceptionTest {

    @Test
    void constructor_setsMessage() {
        AiClientException ex = new AiClientException("erro AI");
        assertEquals("erro AI", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void constructor_setsMessageAndCause() {
        Throwable cause = new RuntimeException("root");
        AiClientException ex = new AiClientException("falha", cause);
        assertEquals("falha", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void acceptsNullMessageAndCause() {
        AiClientException ex = new AiClientException(null, null);
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void preservesCauseInStackTrace() {
        Throwable cause = new IllegalArgumentException("bad");
        AiClientException ex = new AiClientException("outer", cause);

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();

        assertTrue(trace.contains("IllegalArgumentException"));
        assertTrue(trace.contains("outer"));
    }
}
