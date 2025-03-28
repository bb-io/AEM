package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.models.BlackbirdContentStructureModel;
import io.blackbird.aemconnector.core.services.BlackbirdContentStructureService;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.LoginException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class BlackbirdContentStructureServiceImplTest {
    private BlackbirdContentStructureService fixture = new BlackbirdContentStructureServiceImpl();

    @Mock
    private BlackbirdServiceUserResolverProvider resolverProvider;

    @BeforeEach
    void setUp(AemContext context) throws LoginException {
        context.registerService(BlackbirdServiceUserResolverProvider.class, resolverProvider);
        when(resolverProvider.getContentStructureReaderResolver()).thenReturn(context.resourceResolver());
        Map<String, Object> config = Map.of(
                "whiteListPrimaryType", new String[] { "cq:Page", "cq:PageContent", "nt:unstructured" }
        );
        context.registerInjectActivateService(fixture, config);
        context.load().json("/content/base-content.json", "/content");
    }

    @Test
    void getContentStructure(AemContext context) {
        BlackbirdContentStructureModel contentStructure = fixture.getContentStructure("/content/bb-aem-connector/us/en");

        assertNotNull(contentStructure);
    }
}