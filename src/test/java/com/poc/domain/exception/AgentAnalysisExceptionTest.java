package com.poc.domain.exception;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class AgentAnalysisExceptionTest {

    @Test
    void constructor_setsMessageAndCause() {
        Throwable cause = new RuntimeException("root");
        AgentAnalysisException ex = new AgentAnalysisException("failed", cause);

        assertEquals("failed", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertTrue(ex instanceof Exception);
    }

    @Test
    void acceptsNullMessageAndCause() {
        AgentAnalysisException ex = new AgentAnalysisException(null, null);
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void preservesCauseInStackTrace() {
        Throwable cause = new IllegalArgumentException("bad");
        AgentAnalysisException ex = new AgentAnalysisException("outer", cause);

        assertSame(cause, ex.getCause());

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();

        assertTrue(trace.contains("IllegalArgumentException"));
        assertTrue(trace.contains("outer"));
    }
}
