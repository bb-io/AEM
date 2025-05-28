package io.blackbird.aemconnector.core.services.impl.importers;

import com.day.cq.wcm.api.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdPageCopyMergeException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdPageCopyMergeService;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junitx.framework.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class PageImporterTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private BlackbirdPageCopyMergeService pageCopyMergeService;

    @Mock
    private Page mockPage;

    @Mock
    private Resource mockResource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PageImporter pageImporter;

    @BeforeEach
    void setUp() {
        context.registerService(BlackbirdPageCopyMergeService.class, pageCopyMergeService);
        pageImporter = context.registerInjectActivateService(new PageImporter());
    }

    @Test
    void shouldReturnTrueWhenContentTypeIsPage() {
        assertTrue(pageImporter.canImport(ContentType.PAGE));
    }

    @Test
    void shouldReturnFalseWhenContentTypeIsNotPage() {
        assertFalse(pageImporter.canImport(ContentType.UNKNOWN));
    }

    @Test
    void shouldReturnResourceWhenPageImportSuccess() throws Exception {
        String sourcePath = "/content/source";
        String targetPath = "/content/target";
        ObjectNode targetContent = objectMapper.createObjectNode().put("jcr:title", "Test Page");

        when(pageCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent)).thenReturn(mockPage);
        when(mockPage.adaptTo(Resource.class)).thenReturn(mockResource);

        Resource result = pageImporter.importResource(sourcePath, targetPath, targetContent);

        assertNotNull(result);
        assertEquals(mockResource, result);
    }

    @Test
    void shouldThrowsBlackbirdServiceExceptionWhenPageImportFailed() throws Exception {
        String sourcePath = "/content/source";
        String targetPath = "/content/target";
        ObjectNode targetContent = objectMapper.createObjectNode();

        when(pageCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent))
                .thenThrow(new BlackbirdPageCopyMergeException("Merge failed"));

        BlackbirdServiceException exception = assertThrows(BlackbirdServiceException.class,
                () -> pageImporter.importResource(sourcePath, targetPath, targetContent)
        );

        assertEquals("Can not import page, sourcePath: /content/source, targetPath: /content/target", exception.getMessage());
    }

    @Test
    void shouldThrowsBlackbirdServiceExceptionWhenAuthenticationFailed() throws Exception {
        String sourcePath = "/content/source";
        String targetPath = "/content/target";
        ObjectNode targetContent = objectMapper.createObjectNode();

        when(pageCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent))
                .thenThrow(new LoginException("Login failed"));

        assertThrows(BlackbirdServiceException.class,
                () -> pageImporter.importResource(sourcePath, targetPath, targetContent)
        );
    }
}
