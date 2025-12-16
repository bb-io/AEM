package io.blackbird.aemconnector.core.filters.componentwrapper;

import io.blackbird.aemconnector.core.objects.TranslatableContent;
import io.blackbird.aemconnector.core.services.TranslatableDataExtractor;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith({MockitoExtension.class, AemContextExtension.class})
class ComponentWrapperFilterTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    @Mock
    private TranslatableDataExtractor dataExtractor;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ComponentWrapperFilter filter;

    private SlingHttpServletRequest request;
    private SlingHttpServletResponse response;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        Resource resource = context.create().resource("/content/test");
        request = context.request();
        response = context.response();
        context.request().setResource(resource);
    }

    @Test
    void shouldPassThroughForNonSlingRequest() throws Exception {
        ServletRequest nonSlingRequest = mock(ServletRequest.class);
        ServletResponse nonSlingResponse = mock(ServletResponse.class);

        filter.doFilter(nonSlingRequest, nonSlingResponse, filterChain);

        verify(filterChain).doFilter(nonSlingRequest, nonSlingResponse);
        verifyNoInteractions(dataExtractor);
    }

    @Test
    void shouldPassThroughWhenResponseIsCommitted() throws Exception {
        SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);
        when(response.isCommitted()).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(dataExtractor);
    }

    @Test
    void shouldWriteOriginalContentWhenNoTranslatableData() throws Exception {

        doAnswer(invocation -> {
            ServletResponse resp = invocation.getArgument(1);
            resp.getWriter().write("<div>component</div>");
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        String output = context.response().getOutputAsString();
        assertTrue(output.contains("<div>component</div>"));
    }

    @Test
    void shouldWrapComponentWhenTranslatableContentExists() throws Exception {
        String expectedOutput = "<div class=\"blackbird-component-wrapper\" data-bb-translatable='{\"path\":\"/content/test\",\"properties\":{\"foo\":\"foo value\"}}'><p>Hello</p></div>";
        Resource resource = request.getResource();
        String path = resource.getPath();

        TranslatableContent translatableContent = new TranslatableContent(path, Collections.singletonMap("foo", "foo value"));
        Map<String, TranslatableContent> data =
                Collections.singletonMap("/content/test", translatableContent);

        when(dataExtractor.extractFor(any(Node.class))).thenReturn(data);

        doAnswer(invocation -> {
            ServletResponse resp = invocation.getArgument(1);
            resp.getWriter().write("<p>Hello</p>");
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        String output = context.response().getOutputAsString();
        assertEquals(expectedOutput, output);
    }
}