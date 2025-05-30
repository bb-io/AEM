package io.blackbird.aemconnector.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.nio.charset.StandardCharsets;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class ContentImporterServletTest {

    @Mock
    private ContentTypeService contentTypeService;

    @Mock
    private ContentImportService contentImportService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ContentImporterServlet servlet;

    private MockSlingHttpServletRequest request;

    private MockSlingHttpServletResponse response;

    @BeforeEach
    void setup(AemContext context) {
        servlet = new ContentImporterServlet();
        context.registerService(ContentTypeService.class, contentTypeService);
        context.registerService(ContentImportService.class, contentImportService);
        context.registerInjectActivateService(servlet);
        request = context.request();
        response = context.response();
    }

    @Test
    void shouldBuildSuccessResponseWhenRequestBodyIsValid() throws Exception {
        String sourcePath = "/content/bb-aem-connector/us/en/testPage";
        String targetPath = "/content/bb-aem-connector/pl/pl/testPage";
        ObjectNode targetContent = objectMapper.createObjectNode()
                .put("jcr:title", "Title");
        ObjectNode reference = objectMapper.createObjectNode()
                .put("propertyPath", "/jcr:content/root/container")
                .put("propertyName", "pageReference")
                .put("referencePath", "/content/bb-aem-connector/pl/pl/referencePage");
        ArrayNode references = objectMapper.createArrayNode().add(reference);

        ObjectNode payload = objectMapper.createObjectNode()
                .put("sourcePath", sourcePath)
                .put("targetPath", targetPath)
                .set("targetContent", targetContent);
        payload.set("references", references);

        byte[] inputStream = new ByteArrayInputStream(payload.toString().getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        ContentType contentType = mock(ContentType.class);
        Resource resultResource = mock(Resource.class);
        when(resultResource.getPath()).thenReturn(targetPath);
        when(contentTypeService.resolveContentType(sourcePath)).thenReturn(contentType);
        when(contentImportService.importContent(eq(sourcePath), eq(targetPath), any(), any(), eq(contentType))).thenReturn(resultResource);

        Object result = servlet.buildResponsePayload(request, response);

        assertNotNull(result);
        assertTrue(result instanceof ObjectNode);
        ObjectNode resultNode = (ObjectNode) result;
        assertEquals("Content imported successfully", resultNode.get("message").asText());
        assertEquals(targetPath, resultNode.get("path").asText());
    }

    @Test
    void shouldThrowsBlackbirdHttpErrorExceptionWhenRequiredParameterIsMissing() {
        ObjectNode payload = objectMapper.createObjectNode().put("sourcePath", "/content/bb-aem-connector/us/en/testPage");

        byte[] inputStream = new ByteArrayInputStream(payload.toString().getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        BlackbirdHttpErrorException exception = assertThrows(BlackbirdHttpErrorException.class,
                () -> servlet.buildResponsePayload(request, response)
        );

        assertEquals(400, exception.getStatus());
        assertEquals("Missing required fields: sourcePath, targetPath, or targetContent", exception.getMessage());
    }

    @Test
    void shouldThrowsBlackbirdHttpErrorExceptionWhenIOExceptionOccurs() throws Exception {
        SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
        SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);
        when(request.getInputStream()).thenThrow(new IOException("Some exception occurs"));

        BlackbirdHttpErrorException exception = assertThrows(BlackbirdHttpErrorException.class,
                () -> servlet.buildResponsePayload(request, response)
        );

        assertEquals(500, exception.getStatus());
        assertTrue(exception.getMessage().contains("Some exception occurs"));
    }
}
