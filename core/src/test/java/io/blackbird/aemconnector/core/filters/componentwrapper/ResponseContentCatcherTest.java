package io.blackbird.aemconnector.core.filters.componentwrapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ResponseContentCatcherTest {
    private ResponseContentCatcher catcher;

    @BeforeEach
    void setUp() {
        HttpServletResponse originalResponse = mock(HttpServletResponse.class);
        catcher = new ResponseContentCatcher(originalResponse);
    }

    @Test
    void writerUsedShouldBeFalseInitially() {
        assertFalse(catcher.isWriterUsed());
        assertFalse(catcher.hasContent());
    }

    @Test
    void getWriterShouldMarkWriterAsUsed() {
        catcher.getWriter();

        assertTrue(catcher.isWriterUsed());
        assertFalse(catcher.hasContent());
    }

    @Test
    void shouldCaptureWrittenContent() {
        PrintWriter writer = catcher.getWriter();
        writer.write("hello");

        assertTrue(catcher.isWriterUsed());
        assertTrue(catcher.hasContent());
        assertEquals("hello", catcher.getContent());
    }

    @Test
    void closeShouldNotClearBuffer() {
        PrintWriter writer = catcher.getWriter();
        writer.write("content");
        writer.close();

        assertTrue(catcher.hasContent());
        assertEquals("content", catcher.getContent());
    }

    @Test
    void hasContentShouldBeFalseWhenWriterUsedButNothingWritten() {
        catcher.getWriter();

        assertTrue(catcher.isWriterUsed());
        assertFalse(catcher.hasContent());
        assertEquals("", catcher.getContent());
    }
}