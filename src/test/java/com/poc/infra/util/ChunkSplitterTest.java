package com.poc.infra.util;

import jakarta.enterprise.context.ApplicationScoped;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChunkSplitterTest {

    private final ChunkSplitter splitter = new ChunkSplitter();

    @Test
    void nullAndBlankInput_returnsEmptyArray() {
        assertArrayEquals(new String[0], splitter.splitIntoChunks(null, 5));
        assertArrayEquals(new String[0], splitter.splitIntoChunks("", 5));
        assertArrayEquals(new String[0], splitter.splitIntoChunks("   ", 5));
    }

    @Test
    void nonPositiveChunkSize_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> splitter.splitIntoChunks("texto", 0));
        assertThrows(IllegalArgumentException.class, () -> splitter.splitIntoChunks("texto", -1));
    }

    @Test
    void textShorterOrEqualChunkSize_returnsSingleElement() {
        assertArrayEquals(new String[] { "abc" }, splitter.splitIntoChunks("abc", 5));
        assertArrayEquals(new String[] { "abc" }, splitter.splitIntoChunks("abc", 3));
    }

    @Test
    void lineLongerThanChunk_isSplitIntoPieces() {
        String text = "abcdefghij";
        String[] expected = new String[] { "abc", "def", "ghi", "j" };
        assertArrayEquals(expected, splitter.splitIntoChunks(text, 3));
    }

    @Test
    void linesAreCombinedWithNewline_whenFitInChunk() {
        String text = "ab\ncd";
        String[] expected = new String[] { "ab\ncd" };
        assertArrayEquals(expected, splitter.splitIntoChunks(text, 5));
    }

    @Test
    void longLineInMiddle_keepsOrderAndCombinesOthers() {
        String text = "xx\nAAAAAAAAAA\nyy";
        String[] expected = new String[] { "AAAAA", "AAAAA", "xx\nyy" };
        assertArrayEquals(expected, splitter.splitIntoChunks(text, 5));
    }

    @Test
    void classIsAnnotatedWithApplicationScoped() {
        assertTrue(ChunkSplitter.class.isAnnotationPresent(ApplicationScoped.class));
    }
}
