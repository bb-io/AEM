package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.dto.DitaFileImportResponse;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.services.ContentImportService;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.blackbird.aemconnector.core.utils.TestUtils.inputStreamToString;
import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class DitaImporterServletTest {

    @Mock
    private ContentTypeService contentTypeService;

    @Mock
    private ContentImportService contentImportService;

    private DitaImporterServlet servlet;

    private MockSlingHttpServletRequest request;

    private MockSlingHttpServletResponse response;

    @BeforeEach
    void setup(AemContext context) {
        servlet = new DitaImporterServlet();
        context.registerService(ContentTypeService.class, contentTypeService);
        context.registerService(ContentImportService.class, contentImportService);
        context.registerInjectActivateService(servlet);
        request = context.request();
        response = context.response();
    }

    @Test
    void shouldBuildSuccessResponseWhenParamsAreValid() throws Exception {
        String sourcePath = "/content/dam/dita/en/test.dita";
        String targetPath = "/content/dam/dita/pl/test.dita";
        String payload = "<xml>test payload</xml>";

        request.setParameterMap(
                Map.of(
                        "sourcePath", sourcePath,
                        "targetPath", targetPath
                )
        );
        byte[] inputStream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        ContentType contentType = mock(ContentType.class);
        Resource resultResource = mock(Resource.class);
        when(resultResource.getPath()).thenReturn(targetPath);
        when(contentTypeService.resolveContentType(sourcePath)).thenReturn(contentType);
        when(contentImportService.importContent(eq(sourcePath), eq(targetPath), eq(payload), eq(contentType))).thenReturn(resultResource);
        when(resultResource.getPath()).thenReturn(targetPath);

        InputStream result = servlet.buildXmlResponsePayload(request, response);

        assertNotNull(result);
        assertEquals(new DitaFileImportResponse("Content imported successfully", targetPath).toString(), inputStreamToString(result));
    }

    @Test
    void shouldThrowsBlackbirdHttpErrorExceptionWhenRequestParameterIsWrong() {
        String sourcePath = "/content/dam/dita/en/test.dita";
        String targetPath = "content/dam/dita/pl/test.dita";
        String payload = "<xml>test payload</xml>";

        request.setParameterMap(
                Map.of(
                        "sourcePath", sourcePath,
                        "targetPath", targetPath
                )
        );

        byte[] inputStream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        BlackbirdHttpErrorException exception = assertThrows(BlackbirdHttpErrorException.class,
                () -> servlet.buildXmlResponsePayload(request, response)
        );

        assertEquals(400, exception.getStatus());
        assertEquals("targetPath must be an absolute JCR path starting with '/'", exception.getMessage());
    }

    @Test
    void shouldThrowsBlackbirdHttpErrorExceptionWhenRequiredParameterIsMissing() {
        String sourcePath = "/content/dam/dita/en/test.dita";
        String payload = "<xml>test payload</xml>";

        request.setParameterMap(
                Map.of("sourcePath", sourcePath)
        );

        byte[] inputStream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        BlackbirdHttpErrorException exception = assertThrows(BlackbirdHttpErrorException.class,
                () -> servlet.buildXmlResponsePayload(request, response)
        );

        assertEquals(400, exception.getStatus());
        assertEquals("Missing required query parameter: 'targetPath'", exception.getMessage());
    }

    @Test
    void shouldThrowsBlackbirdHttpErrorExceptionWhenPayloadIsMissing() {
        String sourcePath = "/content/dam/dita/en/test.dita";
        String targetPath = "/content/dam/dita/pl/test.dita";
        String payload = "";

        request.setParameterMap(
                Map.of(
                        "sourcePath", sourcePath,
                        "targetPath", targetPath
                )
        );

        byte[] inputStream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        BlackbirdHttpErrorException exception = assertThrows(BlackbirdHttpErrorException.class,
                () -> servlet.buildXmlResponsePayload(request, response)
        );

        assertEquals(400, exception.getStatus());
        assertEquals("Request body is empty. Please add xml data for import.", exception.getMessage());
    }

    @Test
    void shouldThrowsBlackbirdHttpErrorExceptionWhenIOExceptionOccurs() throws Exception {
        String sourcePath = "/content/dam/dita/en/test.dita";
        String targetPath = "/content/dam/dita/pl/test.dita";

        SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
        SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);
        when(request.getInputStream()).thenThrow(new IOException("Some exception occurs"));
        when(request.getParameter("sourcePath")).thenReturn(sourcePath);
        when(request.getParameter("targetPath")).thenReturn(targetPath);

        BlackbirdHttpErrorException exception = assertThrows(BlackbirdHttpErrorException.class,
                () -> servlet.buildXmlResponsePayload(request, response)
        );

        assertEquals(500, exception.getStatus());
        assertEquals("Some exception occurs", exception.getMessage());
    }
}
