package io.blackbird.aemconnector.core.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentImporter;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class ContentImportServiceImplTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private ContentImporter mockImporter;

    @Mock
    private Resource mockResource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ContentImportServiceImpl contentImportService;

    @BeforeEach
    void setUp() {
        contentImportService = context.registerInjectActivateService(new ContentImportServiceImpl());
    }

    @Test
    void shouldReturnResourceWhenSuccessImportPage() throws Exception {
        String sourcePath = "/content/source";
        String targetPath = "/content/target";
        ContentType contentType = ContentType.PAGE;
        ObjectNode targetContent = objectMapper.createObjectNode().put("jcr:title", "Test Page");
        ObjectNode references = objectMapper.createObjectNode();

        when(mockImporter.canImport(contentType)).thenReturn(true);
        when(mockImporter.importResource(sourcePath, targetPath, targetContent, references)).thenReturn(mockResource);

        contentImportService.bindImporter(mockImporter);

        Resource result = contentImportService.importContent(sourcePath, targetPath, targetContent, references, contentType);

        assertNotNull(result);
        assertEquals(mockResource, result);
        verify(mockImporter).importResource(sourcePath, targetPath, targetContent, references);
    }

    @Test
    void shouldReturnResourceWhenSuccessImportExperienceFragment() throws Exception {
        String sourcePath = "/content/source";
        String targetPath = "/content/target";
        ContentType contentType = ContentType.EXPERIENCE_FRAGMENT;
        ObjectNode targetContent = objectMapper.createObjectNode().put("jcr:title", "Test Page");
        ObjectNode references = objectMapper.createObjectNode();

        when(mockImporter.canImport(contentType)).thenReturn(true);
        when(mockImporter.importResource(sourcePath, targetPath, targetContent, references)).thenReturn(mockResource);
        when(mockResource.getPath()).thenReturn(targetPath);

        contentImportService.bindImporter(mockImporter);

        Resource result = contentImportService.importContent(sourcePath, targetPath, targetContent, references, contentType);

        assertNotNull(result);
        assertEquals(mockResource, result);
        verify(mockImporter).importResource(sourcePath, targetPath, targetContent, references);
    }

    @Test
    void shouldThrowsBlackbirdServiceExceptionWhenContentTypeIsUnknown() {
        String sourcePath = "/content/xf/source/variant";
        String targetPath = "/content/xf/target/variant";
        ContentType contentType = ContentType.UNKNOWN;
        ObjectNode targetContent = objectMapper.createObjectNode().put("data", "test");
        ObjectNode references = objectMapper.createObjectNode();

        when(mockImporter.canImport(contentType)).thenReturn(false);

        contentImportService.bindImporter(mockImporter);

        BlackbirdServiceException ex = assertThrows(BlackbirdServiceException.class,
                () -> contentImportService.importContent(sourcePath, targetPath, targetContent, references, contentType)
        );

        assertEquals("No importer for content type: UNKNOWN at /content/xf/source/variant", ex.getMessage());
    }
}
