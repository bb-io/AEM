package io.blackbird.aemconnector.core.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.services.impl.exporters.ExperienceFragmentExporter;
import io.blackbird.aemconnector.core.services.impl.exporters.PageExporter;
import io.blackbird.aemconnector.core.services.v2.ReferenceCollectorService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.Property;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class ContentExportServiceImplTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    @Mock
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;
    @Mock
    private TranslationRulesService translationRulesService;
    @Mock
    ReferenceCollectorService referenceCollectorService;

    private ContentExportServiceImpl target;

    @BeforeEach
    void setUp() throws LoginException, BlackbirdInternalErrorException {
        context.load().json("/content/base-content.json", "/content");

        ResourceResolver spyResolver = Mockito.spy(context.resourceResolver());
        Mockito.doNothing().when(spyResolver).close();

        when(serviceUserResolverProvider.getContentExporterResolver()).thenReturn(spyResolver);
        Mockito.lenient().when(translationRulesService.isTranslatable(any(Property.class))).thenReturn(true);
        Mockito.lenient().when(translationRulesService.isTranslatable(any(Node.class))).thenReturn(TranslationRulesService.IsNodeTranslatable.TRANSLATABLE);

        context.registerService(BlackbirdServiceUserResolverProvider.class, serviceUserResolverProvider);
        context.registerService(TranslationRulesService.class, translationRulesService);

        PageExporter pageExporter = context.registerInjectActivateService(new PageExporter(referenceCollectorService, translationRulesService));
        ExperienceFragmentExporter experienceFragmentExporter = context.registerInjectActivateService(new ExperienceFragmentExporter(referenceCollectorService, translationRulesService));
        target = context.registerInjectActivateService(new ContentExportServiceImpl());

        target.bindExporter(pageExporter);
        target.bindExporter(experienceFragmentExporter);
    }

    @Test
    void testPageExport() throws BlackbirdServiceException {
        final String path = "/content/bb-aem-connector/us/en/pageExport";


        ObjectNode result = (ObjectNode) target.exportContent(path, ContentType.PAGE, Collections.emptyMap());
        JsonNode contentNode = result.get("jcr:content");
        assertNotNull(contentNode);

        JsonNode text = contentNode.get("text");

        assertNotNull(text);

        assertEquals("Hello Page Exporter", text.asText());
    }

    @Test
    void testExperienceFragmentExport() throws BlackbirdServiceException {
        final String path = "/content/experience-fragments/bb-aem-connector/us/en/site/footer/master";
        final String expected = "Hello Experience Fragment Exporter";

        ObjectNode result = (ObjectNode) target.exportContent(path, ContentType.EXPERIENCE_FRAGMENT, Collections.emptyMap());

        JsonNode description = result.at("/jcr:content/root/description");

        assertNotNull(description);

        assertEquals(expected, description.asText());
    }
}