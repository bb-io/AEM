package io.blackbird.aemconnector.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeService;
import io.blackbird.aemconnector.core.services.TagsValidationService;
import io.blackbird.aemconnector.core.services.UpdateTagsService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class UpdateTagsServletTest {

    @Mock
    private ContentType contentType;

    @Mock
    private ContentTypeService contentTypeService;

    @Mock
    private UpdateTagsService updateTagsService;

    @Mock
    private TagsValidationService tagsValidationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UpdateTagsServlet updateTagsServlet;

    private MockSlingHttpServletRequest request;

    private MockSlingHttpServletResponse response;

    @BeforeEach
    void setup(AemContext context) {
        updateTagsServlet = new UpdateTagsServlet();
        context.registerService(ContentTypeService.class, contentTypeService);
        context.registerService(UpdateTagsService.class, updateTagsService);
        context.registerService(TagsValidationService.class, tagsValidationService);
        context.registerInjectActivateService(updateTagsServlet);

        request = context.request();
        response = context.response();
    }

    @Test
    void shouldBuildSuccessResponseWhenParamsAreValid(AemContext context) throws Exception {
        String contentPath = "/content/page";
        Resource updatedResource = context.create().resource(contentPath);

        ArrayNode addTags = objectMapper.createArrayNode().add("tag:test1");
        ArrayNode removeTags = objectMapper.createArrayNode().add("tag:test2");
        ObjectNode payload = objectMapper.createObjectNode().put("contentPath", contentPath);
        payload.set("addTags", addTags);
        payload.set("removeTags", removeTags);
        byte[] inputStream = new ByteArrayInputStream(payload.toString().getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        when(contentTypeService.resolveContentType(contentPath)).thenReturn(contentType);
        when(updateTagsService.updateTags(eq(contentPath), anySet(), anySet(), eq(contentType))).thenReturn(updatedResource);

        Serializable result = updateTagsServlet.buildResponsePayload(request, response);

        assertEquals("Tags updated successfully", ((ObjectNode) result).get("message").asText());
        assertEquals(contentPath, ((ObjectNode) result).get("path").asText());
        verify(tagsValidationService).validateTags(argThat(set -> set.contains("tag:test1") && set.size() == 1));
        verify(tagsValidationService).validateTags(argThat(set -> set.contains("tag:test2") && set.size() == 1));
        verify(updateTagsService, times(1)).updateTags(eq(contentPath), anySet(), anySet(), eq(contentType));
    }

    @Test
    void shouldBuildSuccessResponseWhenRemoveParamIsEmpty(AemContext context) throws Exception {
        String contentPath = "/content/page";
        Resource updatedResource = context.create().resource(contentPath);

        ArrayNode addTags = objectMapper.createArrayNode().add("tag:test1");
        ObjectNode payload = objectMapper.createObjectNode().put("contentPath", contentPath);
        payload.set("addTags", addTags);
        byte[] inputStream = new ByteArrayInputStream(payload.toString().getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        when(contentTypeService.resolveContentType(contentPath)).thenReturn(contentType);
        when(updateTagsService.updateTags(eq(contentPath), anySet(), anySet(), eq(contentType))).thenReturn(updatedResource);

        updateTagsServlet.buildResponsePayload(request, response);

        verify(tagsValidationService, times(1)).validateTags(argThat(set -> set.contains("tag:test1") && set.size() == 1));
        verify(tagsValidationService, never()).validateTags(argThat(set -> set.contains("tag:test2")));
    }

    @Test
    void shouldThrowBlackbirdHttpErrorExceptionWhenAddAndRemoveParamsAreMissing() {
        String contentPath = "/content/page";
        ObjectNode payload = objectMapper.createObjectNode().put("contentPath", contentPath);
        byte[] inputStream = new ByteArrayInputStream(payload.toString().getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        BlackbirdHttpErrorException exception = assertThrows(BlackbirdHttpErrorException.class,
                () -> updateTagsServlet.buildResponsePayload(request, response)
        );

        assertEquals(400, exception.getStatus());
        assertEquals("One of the required tag fields is missing: 'addTags' or 'removeTags'", exception.getMessage());
        verifyNoInteractions(tagsValidationService);
        verifyNoInteractions(updateTagsService);
        verifyNoInteractions(contentTypeService);
    }

    @Test
    void shouldThrowBlackbirdHttpErrorExceptionWhenContentPathIsMissing() {
        ArrayNode addTags = objectMapper.createArrayNode().add("tag:test");
        ArrayNode removeTags = objectMapper.createArrayNode().add("tag:test");
        ObjectNode payload = objectMapper.createObjectNode();
        payload.set("addTags", addTags);
        payload.set("removeTags", removeTags);
        byte[] inputStream = new ByteArrayInputStream(payload.toString().getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        BlackbirdHttpErrorException exception = assertThrows(BlackbirdHttpErrorException.class,
                () -> updateTagsServlet.buildResponsePayload(request, response)
        );

        assertEquals(400, exception.getStatus());
        assertEquals("Missing required field: 'contentPath'", exception.getMessage());
        verifyNoInteractions(tagsValidationService);
        verifyNoInteractions(updateTagsService);
        verifyNoInteractions(contentTypeService);
    }

    @Test
    void shouldThrowBlackbirdHttpErrorExceptionWhenAddAndRemoveParamsHaveSameTag() {
        String contentPath = "/content/page";
        ArrayNode addTags = objectMapper.createArrayNode().add("tag:test");
        ArrayNode removeTags = objectMapper.createArrayNode().add("tag:test");
        ObjectNode payload = objectMapper.createObjectNode().put("contentPath", contentPath);
        payload.set("addTags", addTags);
        payload.set("removeTags", removeTags);
        byte[] inputStream = new ByteArrayInputStream(payload.toString().getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        BlackbirdHttpErrorException exception = assertThrows(BlackbirdHttpErrorException.class,
                () -> updateTagsServlet.buildResponsePayload(request, response)
        );

        assertEquals(400, exception.getStatus());
        assertEquals("Conflicting tags found in both 'addTags' and 'removeTags' values: [tag:test]", exception.getMessage());
        verifyNoInteractions(tagsValidationService);
        verifyNoInteractions(updateTagsService);
        verifyNoInteractions(contentTypeService);
    }

    @Test
    void shouldThrowBlackbirdHttpErrorExceptionWhenServiceThrowsBlackbirdServiceException() {
        String contentPath = "/content/page";
        ArrayNode addTags = objectMapper.createArrayNode().add("tag:test");
        ObjectNode payload = objectMapper.createObjectNode().put("contentPath", contentPath);
        payload.set("addTags", addTags);
        byte[] inputStream = new ByteArrayInputStream(payload.toString().getBytes(StandardCharsets.UTF_8)).readAllBytes();
        request.setContent(inputStream);

        when(contentTypeService.resolveContentType(contentPath)).thenReturn(contentType);
        when(updateTagsService.updateTags(eq(contentPath), anySet(), anySet(), eq(contentType)))
                .thenThrow(new BlackbirdServiceException("Some error occurs"));

        BlackbirdHttpErrorException exception = assertThrows(BlackbirdHttpErrorException.class,
                () -> updateTagsServlet.buildResponsePayload(request, response)
        );

        assertEquals(500, exception.getStatus());
        assertEquals("Some error occurs", exception.getMessage());
    }

}
