package com.poc.domain.exception;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class ProjectScanExceptionTest {

    @Test
    void constructor_setsMessage() {
        ProjectScanException ex = new ProjectScanException("scan error");
        assertEquals("scan error", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void constructor_setsMessageAndCause() {
        Throwable cause = new RuntimeException("root");
        ProjectScanException ex = new ProjectScanException("failed", cause);
        assertEquals("failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void acceptsNullMessageAndCause() {
        ProjectScanException ex = new ProjectScanException(null, null);
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void preservesCauseInStackTrace() {
        Throwable cause = new IllegalArgumentException("bad");
        ProjectScanException ex = new ProjectScanException("outer", cause);

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();

        assertTrue(trace.contains("IllegalArgumentException"));
        assertTrue(trace.contains("outer"));
    }
}
