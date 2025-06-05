package io.blackbird.aemconnector.core.services.v2.impl;

import io.blackbird.aemconnector.core.dto.v2.ContentReference;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.RepositoryException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class ContentReferenceCollectorImplTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    @Mock
    private TranslationRulesService translationRulesService;

    private ContentReferenceCollectorImpl contentReferenceCollector;

    @BeforeEach
    void setUp() throws BlackbirdInternalErrorException {
        Mockito.lenient().when(translationRulesService.isAssetReference(argThat(property -> {
            try {
                String name = property.getName();
                return name.startsWith("ref");
            } catch (RepositoryException e) {
                return false;
            }

        }))).thenReturn(TranslationRulesService.IsAssetReference.REFERENCE_WITH_CHILDREN);
        context.registerService(TranslationRulesService.class, translationRulesService);
        contentReferenceCollector = context.registerInjectActivateService(new ContentReferenceCollectorImpl());
    }

    @Test
    void testReferenceCollector() {
        context.create().resource("/content/node1", "ref1", "/one/two", "ref2", "/three/four");
        context.create().resource("/content/node1/node2", "ref3", "/five/six", "ref4", "/seven/eight");
        context.create().resource("/content/node1/node2/node3", "ref5", "/nine/ten", "ref6", "/eleven/twelve");

        Resource resource = context.resourceResolver().getResource("/content/node1");

        List<ContentReference> references = contentReferenceCollector.getReferences(resource);

        assertEquals(6, references.size());
    }
}