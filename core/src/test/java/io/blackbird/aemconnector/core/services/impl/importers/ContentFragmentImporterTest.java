package io.blackbird.aemconnector.core.services.impl.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdAssetCopyMergeService;
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
import static junit.framework.Assert.assertTrue;
import static junitx.framework.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class ContentFragmentImporterTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private BlackbirdAssetCopyMergeService cfCopyMergeService;

    @Mock
    private Resource mockResource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ContentFragmentImporter cfImporter;

    @BeforeEach
    void setUp() {
        context.registerService(BlackbirdAssetCopyMergeService.class, cfCopyMergeService);
        cfImporter = context.registerInjectActivateService(new ContentFragmentImporter());
    }

    @Test
    void shouldReturnTrueWhenContentTypeIsContentFragment() {
        assertTrue(cfImporter.canImport(ContentType.CONTENT_FRAGMENT));
    }

    @Test
    void shouldReturnFalseWhenContentTypeIsNotContentFragment() {
        assertFalse(cfImporter.canImport(ContentType.UNKNOWN));
    }

    @Test
    void shouldReturnResourceWhenContentFragmentImportSuccess() throws Exception {
        String sourcePath = "/content/cf/source/variant";
        String targetPath = "/content/cf/target/variant";
        ObjectNode targetContent = objectMapper.createObjectNode().put("data", "test");
        ObjectNode references = objectMapper.createObjectNode();

        when(cfCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent, references)).thenReturn(mockResource);

        Resource result = cfImporter.importResource(sourcePath, targetPath, targetContent, references);

        assertNotNull(result);
        assertEquals(mockResource, result);
    }

    @Test
    void shouldThrowsBlackbirdServiceExceptionWhenContentFragmentImportFailed() throws Exception {
        String sourcePath = "/content/cf/source/variant";
        String targetPath = "/content/cf/target/variant";
        ObjectNode targetContent = objectMapper.createObjectNode();
        ObjectNode references = objectMapper.createObjectNode();

        when(cfCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent, references))
                .thenThrow(new BlackbirdResourceCopyMergeException("Merge failed"));

        BlackbirdServiceException exception = assertThrows(BlackbirdServiceException.class,
                () -> cfImporter.importResource(sourcePath, targetPath, targetContent, references)
        );

        assertEquals("Can not import content fragment, sourcePath: /content/cf/source/variant, " +
                "targetPath: /content/cf/target/variant", exception.getMessage());
    }
}
