package io.blackbird.aemconnector.core.services.impl;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class BlackbirdServiceUserResolverProviderImplTest {

    @Captor
    ArgumentCaptor<Map<String, Object>> captor;
    @Mock
    private ResourceResolverFactory resourceResolverFactory;
    @InjectMocks
    private BlackbirdServiceUserResolverProviderImpl target;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldReturnContentStructureReaderResourceResolver() throws LoginException {
        // GIVEN

        // WHEN
        target.getContentStructureReaderResolver();

        // THEN
        Mockito.verify(resourceResolverFactory).getServiceResourceResolver(captor.capture());
        Assertions.assertEquals("blackbird-content-structure-reader-service", captor.getValue().get(ResourceResolverFactory.SUBSERVICE));
    }

    @Test
    void shouldReturnUpdatesReaderResourceResolver() throws LoginException {
        // GIVEN

        // WHEN
        target.getUpdatesReaderResolver();

        // THEN
        Mockito.verify(resourceResolverFactory).getServiceResourceResolver(captor.capture());
        Assertions.assertEquals("blackbird-updates-reader-service", captor.getValue().get(ResourceResolverFactory.SUBSERVICE));
    }

    @Test
    void shouldReturnPageContentReaderResourceResolver() throws LoginException {
        // GIVEN

        // WHEN
        target.getPageContentReaderResolver();

        // THEN
        Mockito.verify(resourceResolverFactory).getServiceResourceResolver(captor.capture());
        Assertions.assertEquals("blackbird-page-content-reader-service", captor.getValue().get(ResourceResolverFactory.SUBSERVICE));
    }

    @Test
    void shouldReturnTranslationWriterResourceResolver() throws LoginException {
        // GIVEN

        // WHEN
        target.getTranslationWriterResolver();

        // THEN
        Mockito.verify(resourceResolverFactory).getServiceResourceResolver(captor.capture());
        Assertions.assertEquals("blackbird-translation-writer-service", captor.getValue().get(ResourceResolverFactory.SUBSERVICE));
    }

}
