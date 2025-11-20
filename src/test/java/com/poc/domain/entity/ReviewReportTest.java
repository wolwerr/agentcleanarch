package com.poc.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewReportTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void details_accessor_returnsSameNode() throws Exception {
        JsonNode node = mapper.readTree("{\"a\":1}");
        ReviewReport report = new ReviewReport(node);
        assertSame(node, report.details());
    }

    @Test
    void equals_and_hashCode_basedOnJsonNodeStructure() throws Exception {
        JsonNode node1 = mapper.readTree("{\"a\":1}");
        JsonNode node2 = mapper.readTree("{\"a\":1}");
        ReviewReport r1 = new ReviewReport(node1);
        ReviewReport r2 = new ReviewReport(node2);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void toString_containsJsonContent() throws Exception {
        JsonNode node = mapper.readTree("{\"hello\":\"world\"}");
        ReviewReport report = new ReviewReport(node);
        assertTrue(report.toString().contains("\"hello\":\"world\""));
    }

    @Test
    void allowsNullDetails() {
        ReviewReport report = new ReviewReport(null);
        assertNull(report.details());
        assertTrue(report.toString().contains("null"));
    }
}
