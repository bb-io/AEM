package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentExportService;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import static io.blackbird.aemconnector.core.utils.TestUtils.inputStreamToString;
import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class DitaExporterServletTest {

    @Mock
    private ContentTypeService contentTypeService;

    @Mock
    private ContentExportService contentExportService;

    private DitaExporterServlet servlet;

    private MockSlingHttpServletRequest request;
    private MockSlingHttpServletResponse response;

    @BeforeEach
    void setup(AemContext context) {
        servlet = new DitaExporterServlet();
        context.registerService(ContentTypeService.class, contentTypeService);
        context.registerService(ContentExportService.class, contentExportService);
        context.registerInjectActivateService(servlet);

        request = context.request();
        response = context.response();
    }

    @Test
    void shouldBuildSuccessResponseWhenParamsAreValid() throws Exception {
        String contentPath = "/content/dam/dita/en/test.dita";
        Serializable exported = "<xml>test</xml>";

        request.setParameterMap(Map.of("contentPath", contentPath));

        ContentType contentType = mock(ContentType.class);
        when(contentTypeService.resolveContentType(contentPath)).thenReturn(contentType);
        when(contentExportService.exportContent(eq(contentPath), eq(contentType), anyMap())).thenReturn(exported);

        InputStream result = servlet.buildXmlResponsePayload(request, response);

        assertNotNull(result);
        assertEquals(exported, inputStreamToString(result));
    }


    @Test
    void shouldThrowBlackbirdHttpErrorExceptionWhenContentPathIsMissing() {
        request.setParameterMap(Map.of());

        BlackbirdHttpErrorException ex = assertThrows(BlackbirdHttpErrorException.class,
                () -> servlet.buildResponsePayload(request, response)
        );

        assertEquals(400, ex.getStatus());
        assertEquals("Missing required query parameter: 'contentPath'", ex.getMessage());
    }

    @Test
    void shouldThrowBlackbirdHttpErrorExceptionWhenResourceNotFound() {
        String contentPath = "/content/dam/missing.dita";

        request.setParameterMap(Map.of("contentPath", contentPath));

        when(contentTypeService.resolveContentType(contentPath)).thenReturn(mock(ContentType.class));
        when(contentExportService.exportContent(eq(contentPath), any(), anyMap())).thenThrow(new BlackbirdServiceException(
                String.format("No resource found at path: %s", contentPath)
        ));

        BlackbirdHttpErrorException ex = assertThrows(BlackbirdHttpErrorException.class,
                () -> servlet.buildResponsePayload(request, response)
        );

        assertEquals(404, ex.getStatus());
        assertEquals("No resource found at path: /content/dam/missing.dita", ex.getMessage());
    }

    @Test
    void shouldThrowexceptionWhenBlackbirdServiceExceptionOccurs() {
        String contentPath = "/content/dam/dita/en/test.dita";

        request.setParameterMap(Map.of("contentPath", contentPath));
        when(contentTypeService.resolveContentType(contentPath)).thenReturn(mock(ContentType.class));
        when(contentExportService.exportContent(eq(contentPath), any(), anyMap()))
                .thenThrow(new BlackbirdServiceException("BlackbirdServiceException occurs"));

        BlackbirdHttpErrorException ex = assertThrows(BlackbirdHttpErrorException.class,
                () -> servlet.buildResponsePayload(request, response)
        );

        assertEquals(500, ex.getStatus());
        assertEquals("BlackbirdServiceException occurs", ex.getMessage());
    }

    @Test
    void shouldInjectServletTypeWhenTypeIsXml() throws Exception {
        String contentPath = "/content/dam/dita/en/test.dita";
        Serializable exported = "<xml>test</xml>";

        request.setParameterMap(Map.of("contentPath", contentPath));

        ContentType contentType = mock(ContentType.class);
        when(contentTypeService.resolveContentType(contentPath)).thenReturn(contentType);

        doAnswer(invocation -> {
            Map<String, Object> options = invocation.getArgument(2);
            assertEquals("xml", options.get("type"));
            return exported;
        }).when(contentExportService).exportContent(eq(contentPath), eq(contentType), anyMap());

        InputStream result = servlet.buildXmlResponsePayload(request, response);

        assertEquals(exported, inputStreamToString(result));
    }
}
