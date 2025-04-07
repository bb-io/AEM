package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.models.configs.PageContentFilterConfig;
import io.blackbird.aemconnector.core.services.BlackbirdConnectorConfigurationService;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class BlackbirdConnectorConfigurationServiceImplTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private BlackbirdServiceUserResolverProvider resolverProvider;
    @Mock
    private ResourceResolver resourceResolver;

    @InjectMocks
    private BlackbirdConnectorConfigurationServiceImpl target;

    @BeforeEach
    void setUp() throws LoginException {
        context.registerService(BlackbirdServiceUserResolverProvider.class, resolverProvider);
        when(resolverProvider.getConfigurationResolver()).thenReturn(resourceResolver);
    }

    @Test
    void shouldReturnNullWhenConfigResourceNotFound() {
        // GIVEN
        when(resourceResolver.getResource(BlackbirdConnectorConfigurationService.CONFIG_PATH_DEFAULT)).thenReturn(null);

        // WHEN
        PageContentFilterConfig result = target.getPageContentFilterConfig();

        // THEN
        assertNull(result);
        verify(resourceResolver).getResource(BlackbirdConnectorConfigurationService.CONFIG_PATH_DEFAULT);
    }

    @Test
    void shouldReturnNullWhenLoginExceptionOccurs() throws LoginException {
        // GIVEN
        when(resolverProvider.getConfigurationResolver()).thenThrow(new LoginException("Test exception"));

        // WHEN
        PageContentFilterConfig result = target.getPageContentFilterConfig();

        // THEN
        assertNull(result);
    }

    @Test
    void shouldReturnConfig() throws LoginException {
        // GIVEN
        context.load().json("/conf/blackbird-connector-config.json", "/conf");
        when(resolverProvider.getConfigurationResolver()).thenReturn(context.resourceResolver());

        // WHEN
        PageContentFilterConfig result = target.getPageContentFilterConfig();

        // THEN
        assertNotNull(result);
        assertNotNull(result.getBlacklistedPropertyNames());
        assertNotNull(result.getBlacklistedNodeNames());
        assertEquals(2, result.getBlacklistedPropertyNames().size());
        assertEquals(1, result.getBlacklistedNodeNames().size());
        assertEquals("jcr:lastModified", result.getBlacklistedPropertyNames().get(0).getPropertyName());
        assertEquals("jcr:created", result.getBlacklistedPropertyNames().get(1).getPropertyName());
        assertEquals("rep:policy", result.getBlacklistedNodeNames().get(0).getPropertyName());
    }

}
