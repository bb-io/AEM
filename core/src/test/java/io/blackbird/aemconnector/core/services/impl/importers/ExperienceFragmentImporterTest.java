package io.blackbird.aemconnector.core.services.impl.importers;

import com.day.cq.wcm.api.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdPageCopyMergeService;
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
public class ExperienceFragmentImporterTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private BlackbirdPageCopyMergeService pageCopyMergeService;

    @Mock
    private Page mockPage;

    @Mock
    private Resource mockResource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ExperienceFragmentImporter xfImporter;

    @BeforeEach
    void setUp() {
        context.registerService(BlackbirdPageCopyMergeService.class, pageCopyMergeService);
        xfImporter = context.registerInjectActivateService(new ExperienceFragmentImporter());
    }

    @Test
    void shouldReturnTrueWhenContentTypeIsExperienceFragment() {
        assertTrue(xfImporter.canImport(ContentType.EXPERIENCE_FRAGMENT));
    }

    @Test
    void shouldReturnFalseWhenContentTypeIsNotExperienceFragment() {
        assertFalse(xfImporter.canImport(ContentType.UNKNOWN));
    }

    @Test
    void shouldReturnResourceWhenExperienceFragmentImportSuccess() throws Exception {
        String sourcePath = "/content/xf/source/variant";
        String targetPath = "/content/xf/target/variant";
        ObjectNode targetContent = objectMapper.createObjectNode().put("data", "test");
        ObjectNode references = objectMapper.createObjectNode();

        when(pageCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent, references)).thenReturn(mockPage);
        when(mockPage.adaptTo(Resource.class)).thenReturn(mockResource);

        Resource result = xfImporter.importResource(sourcePath, targetPath, targetContent, references);

        assertNotNull(result);
        assertEquals(mockResource, result);
    }

    @Test
    void shouldThrowsBlackbirdServiceExceptionWhenExperienceFragmentImportFailed() throws Exception {
        String sourcePath = "/content/xf/source/variant";
        String targetPath = "/content/xf/target/variant";
        ObjectNode targetContent = objectMapper.createObjectNode();
        ObjectNode references = objectMapper.createObjectNode();

        when(pageCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent, references))
                .thenThrow(new BlackbirdResourceCopyMergeException("Merge failed"));

        BlackbirdServiceException exception = assertThrows(BlackbirdServiceException.class,
                () -> xfImporter.importResource(sourcePath, targetPath, targetContent, references)
        );

        assertEquals("Can not import experience fragment, sourcePath: /content/xf/source/variant, " +
                "targetPath: /content/xf/target/variant", exception.getMessage());
    }
}
