package io.blackbird.aemconnector.core.servlets.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.servlets.post.JSONResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BlackbirdAbstractBaseServletTest {

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private SlingHttpServletResponse response;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    private BlackbirdAbstractBaseServlet servlet;

    @BeforeEach
    public void setUp() throws IOException {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    public void testDoGetValid() throws IOException {
        servlet = new BlackbirdAbstractBaseServlet() {
            @Override
            public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) {
                Map<String, String> dummyData = new HashMap<>();
                dummyData.put("key", "value");
                return (Serializable) dummyData;
            }
        };

        when(response.getWriter()).thenReturn(printWriter);
        servlet.doGet(request, response);

        verify(response).setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
        verify(response).setStatus(HttpServletResponse.SC_OK);

        printWriter.flush();

        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> result = mapper.readValue(stringWriter.toString(), Map.class);
        assertEquals("value", result.get("key"));
    }

    @Test
    public void testDoGetSerializableNull() throws IOException {
        servlet = new BlackbirdAbstractBaseServlet() {
            @Override
            public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) {
                return null;
            }
        };

        when(response.getWriter()).thenReturn(printWriter);

        servlet.doGet(request, response);

        verify(response, atLeastOnce()).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}