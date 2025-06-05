package io.blackbird.aemconnector.core.services.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.services.BlackbirdPageCopyMergeService;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.stubs.PageManagerStub;
import io.blackbird.aemconnector.core.stubs.ResourceResolverStub;
import io.blackbird.aemconnector.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class BlackbirdPageCopyMergeServiceImplTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private BlackbirdServiceUserResolverProvider resolverProvider;

    ObjectMapper objectMapper = new ObjectMapper();

    BlackbirdPageCopyMergeService target;

    @BeforeEach
    void setUp() throws LoginException {
        PageManagerStub pageManagerStub = new PageManagerStub(context.resourceResolver(), context.pageManager());
        ResourceResolverStub resourceResolverStub = new ResourceResolverStub(context.resourceResolver(), pageManagerStub);

        context.registerAdapter(ResourceResolver.class, PageManager.class, pageManagerStub);

        when(resolverProvider.getTranslationWriterResolver()).thenReturn(resourceResolverStub);

        context.registerService(BlackbirdServiceUserResolverProvider.class, resolverProvider);
        context.load().json("/content/base-content.json", "/content");

        target = context.registerInjectActivateService(new BlackbirdPageCopyMergeServiceImpl());
    }

    @Test
    void shouldCopyAndMergeContentWhenTargetPageNotExist() throws BlackbirdResourceCopyMergeException, JsonProcessingException {
        String sourcePath = "/content/bb-aem-connector/us/en/testPage";
        String targetPath = "/content/bb-aem-connector/pl/pl/testPage";
        String jsonStr = "{\n" +
                "  \"jcr:content\": {\n" +
                "    \"jcr:title\": \"Strona kategorii\",\n" +
                "    \"text\": \"Witamy w hotelu California\"\n" +
                "  }\n" +
                "}";
        JsonNode jsonNode = objectMapper.readTree(jsonStr);

        Page page = target.copyAndMerge(sourcePath, targetPath, jsonNode, null);

        ValueMap properties = page.getProperties();

        assertEquals(targetPath, page.getPath());
        assertEquals(
                jsonNode.get("jcr:content").get("text").asText(),
                properties.get("text", String.class));
        assertEquals(
                jsonNode.get("jcr:content").get("jcr:title").asText(),
                properties.get("jcr:title", String.class));
    }

    @Test
    void shouldCopyAndMergeContentAndUpdateReferencesWhenTargetPageNotExist() throws BlackbirdResourceCopyMergeException, JsonProcessingException {
        String sourcePath = "/content/bb-aem-connector/us/en/testPage";
        String targetPath = "/content/bb-aem-connector/pl/pl/testPage";
        String jsonStr = "{\n" +
                "  \"jcr:content\": {\n" +
                "    \"jcr:title\": \"Strona kategorii\",\n" +
                "    \"text\": \"Witamy w hotelu California\"\n" +
                "  }\n" +
                "}";
        JsonNode jsonNode = objectMapper.readTree(jsonStr);
        String referencesStr = "[{\"propertyPath\":\"/jcr:content\",\"propertyName\":\"pageReference\",\"referencePath\":\"/content/xf/bb-aem-connector/pl/pl/header/master\"}]";
        JsonNode references = objectMapper.readTree(referencesStr);

        Page page = target.copyAndMerge(sourcePath, targetPath, jsonNode, references);

        ValueMap properties = page.getProperties();

        assertEquals(targetPath, page.getPath());
        assertEquals(
                jsonNode.get("jcr:content").get("text").asText(),
                properties.get("text", String.class));
        assertEquals(
                jsonNode.get("jcr:content").get("jcr:title").asText(),
                properties.get("jcr:title", String.class));
        assertEquals(
                references.get(0).get("referencePath").asText(),
                properties.get("pageReference", String.class));
    }
}