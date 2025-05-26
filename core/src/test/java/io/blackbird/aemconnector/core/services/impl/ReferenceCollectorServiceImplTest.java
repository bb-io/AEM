package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.dto.ContentReference;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.ReferenceCollectorService;
import io.blackbird.aemconnector.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class ReferenceCollectorServiceImplTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private BlackbirdServiceUserResolverProvider resolverProvider;

    private ReferenceCollectorService target;

    @BeforeEach
    void setUp() throws Exception {
        context.load().json("/content/base-content.json", "/content");
        context.registerService(BlackbirdServiceUserResolverProvider.class, resolverProvider);
        target = context.registerInjectActivateService(new ReferenceCollectorServiceImpl());
        when(resolverProvider.getReferenceReaderResolver()).thenReturn(context.resourceResolver());
    }

    @Test
    void shouldReturnEmptyReferencesWhenPageNotExists() {
        List<ContentReference> result = target.getReferences("/content/non-existing-page");

        assertEquals(0, result.size());
    }

    @Test
    void shouldReturnReferencesWhenPageContainsReferences() {
        context.create().resource("/content/reference/image");
        context.create().resource("/content/dam/reference/content-fragment");
        context.create().resource("/content/reference/experience-fragments");

        List<ContentReference> result = target.getReferences("/content/bb-aem-connector");

        assertEquals(4, result.size());
        assertTrue(result.stream().anyMatch(reference -> reference.getPath().equals("/content/reference/image")));
        assertTrue(result.stream().anyMatch(reference -> reference.getPath().equals("/content/bb-aem-connector/us/en/products")));
        assertTrue(result.stream().anyMatch(reference -> reference.getPath().equals("/content/dam/reference/content-fragment")));
        assertTrue(result.stream().anyMatch(reference -> reference.getPath().equals("/content/reference/experience-fragments")));
    }

    @Test
    void shouldReturnReferencesWhenTemplateContainsReferences() {
        context.create().resource("/content/experience-fragments/header/master");
        context.create().resource("/conf/bb-aem-connector/settings/wcm/templates/page-content", "jcr:primaryType", "cq:Template");
        context.create().resource("/conf/bb-aem-connector/settings/wcm/templates/page-content/structure/jcr:content");
        context.create().resource("/conf/bb-aem-connector/settings/wcm/templates/page-content/structure/jcr:content/experiencefragment-header", "fragmentVariationPath", "/content/experience-fragments/header/master");

        List<ContentReference> result = target.getReferences("/content/bb-aem-connector");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(reference -> reference.getPath().equals("/content/bb-aem-connector/us/en/products")));
        assertTrue(result.stream().anyMatch(reference -> reference.getPath().equals("/content/experience-fragments/header/master")));
    }

}
