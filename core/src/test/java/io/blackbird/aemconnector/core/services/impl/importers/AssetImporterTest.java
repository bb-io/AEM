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
public class AssetImporterTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private BlackbirdAssetCopyMergeService cfCopyMergeService;

    @Mock
    private Resource mockResource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AssetImporter assetImporter;

    @BeforeEach
    void setUp() {
        context.registerService(BlackbirdAssetCopyMergeService.class, cfCopyMergeService);
        assetImporter = context.registerInjectActivateService(new AssetImporter());
    }

    @Test
    void shouldReturnTrueWhenContentTypeIsAsset() {
        assertTrue(assetImporter.canImport(ContentType.ASSET));
    }

    @Test
    void shouldReturnFalseWhenContentTypeIsNotAsset() {
        assertFalse(assetImporter.canImport(ContentType.UNKNOWN));
    }

    @Test
    void shouldReturnResourceWhenAssetImportSuccess() throws Exception {
        String sourcePath = "/content/dam/source/asset";
        String targetPath = "/content/dar/target/asset";
        ObjectNode targetContent = objectMapper.createObjectNode().put("data", "test");
        ObjectNode references = objectMapper.createObjectNode();

        when(cfCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent, references)).thenReturn(mockResource);

        Resource result = assetImporter.importResource(sourcePath, targetPath, targetContent, references);

        assertNotNull(result);
        assertEquals(mockResource, result);
    }

    @Test
    void shouldThrowsBlackbirdServiceExceptionWhenAssetImportFailed() throws Exception {
        String sourcePath = "/content/dam/source/asset";
        String targetPath = "/content/dar/target/asset";
        ObjectNode targetContent = objectMapper.createObjectNode();
        ObjectNode references = objectMapper.createObjectNode();

        when(cfCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent, references))
                .thenThrow(new BlackbirdResourceCopyMergeException("Merge failed"));

        BlackbirdServiceException exception = assertThrows(BlackbirdServiceException.class,
                () -> assetImporter.importResource(sourcePath, targetPath, targetContent, references)
        );

        assertEquals("Can not import asset, sourcePath: /content/dam/source/asset, " +
                "targetPath: /content/dar/target/asset", exception.getMessage());
    }
}
