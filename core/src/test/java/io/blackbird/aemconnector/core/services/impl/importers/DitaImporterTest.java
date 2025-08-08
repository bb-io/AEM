package io.blackbird.aemconnector.core.services.impl.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.DitaCopyMergeService;
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
public class DitaImporterTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private DitaCopyMergeService ditaCopyMergeService;

    @Mock
    private Resource mockResource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DitaImporter ditaImporter;

    @BeforeEach
    void setUp() {
        context.registerService(DitaCopyMergeService.class, ditaCopyMergeService);
        ditaImporter = context.registerInjectActivateService(new DitaImporter());
    }

    @Test
    void shouldReturnTrueWhenContentTypeIsDita() {
        assertTrue(ditaImporter.canImport(ContentType.DITA));
    }

    @Test
    void shouldReturnFalseWhenContentTypeIsNotDita() {
        assertFalse(ditaImporter.canImport(ContentType.UNKNOWN));
    }

    @Test
    void shouldReturnResourceWhenDitaImportSuccess() throws Exception {
        String sourcePath = "/content/dam/source/dita";
        String targetPath = "/content/dar/target/dita";
        ObjectNode targetContent = objectMapper.createObjectNode().put("data", "test");
        ObjectNode references = objectMapper.createObjectNode();

        when(ditaCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent, references)).thenReturn(mockResource);

        Resource result = ditaImporter.importResource(sourcePath, targetPath, targetContent, references);

        assertNotNull(result);
        assertEquals(mockResource, result);
    }

    @Test
    void shouldThrowsBlackbirdServiceExceptionWhenDitaImportFailed() throws Exception {
        String sourcePath = "/content/dam/source/dita";
        String targetPath = "/content/dar/target/dita";
        ObjectNode targetContent = objectMapper.createObjectNode();
        ObjectNode references = objectMapper.createObjectNode();

        when(ditaCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent, references))
                .thenThrow(new BlackbirdResourceCopyMergeException("Merge failed"));

        BlackbirdServiceException exception = assertThrows(BlackbirdServiceException.class,
                () -> ditaImporter.importResource(sourcePath, targetPath, targetContent, references)
        );

        assertEquals("Can not import dita file, sourcePath: /content/dam/source/dita, " +
                "targetPath: /content/dar/target/dita", exception.getMessage());
    }
}
