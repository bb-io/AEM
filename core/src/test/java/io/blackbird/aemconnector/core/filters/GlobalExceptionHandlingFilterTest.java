package io.blackbird.aemconnector.core.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.blackbird.aemconnector.core.dto.BlackbirdErrorResponse;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import io.blackbird.aemconnector.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.http.HttpStatus;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class GlobalExceptionHandlingFilterTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private FilterChain filterChain;
    private GlobalExceptionHandlingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new GlobalExceptionHandlingFilter();
    }

    @Test
    void testSuccessfulFilter() throws ServletException, IOException {
        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);

        int status = response.getStatus();
        assertEquals(HttpStatus.SC_OK, status);
    }

    @Test
    void testFilterWithException() throws ServletException, IOException {
        String errorMessage = "Failed to export content";
        doThrow(new RuntimeException(errorMessage)).when(filterChain).doFilter(any(), any());

        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();
        MockRequestPathInfo requestPathInfo = (MockRequestPathInfo) request.getRequestPathInfo();

        String requestPath = "/content/services/bb-aem-connector/content-exporter";
        requestPathInfo.setResourcePath(requestPath);

        filter.doFilter(request, response, filterChain);

        int status = response.getStatus();
        String jsonResponse = response.getOutputAsString();

        ObjectMapper objectMapper = BlackbirdAbstractBaseServlet.getObjectMapper();
        BlackbirdErrorResponse error = objectMapper.readValue(jsonResponse, BlackbirdErrorResponse.class);

        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, error.getStatus());
        assertEquals(requestPath, error.getPath());
        assertEquals(errorMessage, error.getMessage());
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, status);
    }
}