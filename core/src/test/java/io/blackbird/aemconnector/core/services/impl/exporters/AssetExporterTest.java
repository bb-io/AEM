package io.blackbird.aemconnector.core.services.impl.exporters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.constants.ServletConstants;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.utils.Node2JsonUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import javax.jcr.Node;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({MockitoExtension.class, AemContextExtension.class})
class AssetExporterTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    @Mock
    private TranslationRulesService translationRulesService;
    @InjectMocks
    private AssetExporter assetExporter;

    @BeforeEach
    void setUp() {
        context.load().json("/content/test-asset.json", "/asset");
    }

    @Test
    void shouldBeAbleToExportAsset() {
        assertTrue(assetExporter.canExport(ContentType.ASSET));
    }

    @Test
    void shouldExportEntireResourceIgnoringTranslationRules() throws IOException {

        Map<String, Object> options = Collections.singletonMap(ServletConstants.IGNORE_TRANSLATION_RULES, true);

        Resource resource = context.resourceResolver().getResource("/asset");

        JsonNode actual = (JsonNode) assetExporter.export(resource, options);
        JsonNode expected = getJsonNodeByResourcePath();

        JSONAssert.assertEquals(actual.toPrettyString(), expected.toPrettyString(), JSONCompareMode.LENIENT);
    }

    @Test
    void shouldExportTranslatableProperties() {
        try (MockedStatic<Node2JsonUtil> utilities = Mockito.mockStatic(Node2JsonUtil.class)) {

            Resource resource = context.resourceResolver().getResource("/asset");
            Node node = resource.adaptTo(Node.class);

            ObjectNode mockedJson = new ObjectMapper().createObjectNode();
            mockedJson.put("title", "Translatable title");

            utilities
                    .when(() -> Node2JsonUtil.serializeRecursively(node, translationRulesService))
                    .thenReturn(mockedJson);


            Serializable result = assetExporter.export(resource, Collections.emptyMap());

            assertInstanceOf(ObjectNode.class, result);
            assertEquals(mockedJson, result);
        }
    }

    @Test
    void shouldThrowBlackbirdServiceExceptionWhenSerializationFails() throws Exception {
        Resource resource = context.resourceResolver().getResource("/asset");
        assertNotNull(resource);
        Node node = resource.adaptTo(Node.class);

        try (MockedStatic<Node2JsonUtil> utilities = Mockito.mockStatic(Node2JsonUtil.class)) {

            utilities.when(() -> Node2JsonUtil.serializeRecursively(
                    Mockito.eq(node),
                    Mockito.eq(translationRulesService)
            )).thenThrow(new BlackbirdInternalErrorException("Serialization error"));

            BlackbirdServiceException exception = assertThrows(
                    BlackbirdServiceException.class,
                    () -> assetExporter.export(resource, Collections.emptyMap())
            );

            assertTrue(exception.getMessage().contains("Serialization error"));
            assertNotNull(exception.getCause());
            assertInstanceOf(BlackbirdInternalErrorException.class, exception.getCause());
        }
    }

    private JsonNode getJsonNodeByResourcePath() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("content/test-asset.json")) {
            assertNotNull(is, "Resource not found: content/test-asset.json");
            ObjectMapper mapper = Node2JsonUtil.getMapper();
            return mapper.readTree(is);
        }
    }
}