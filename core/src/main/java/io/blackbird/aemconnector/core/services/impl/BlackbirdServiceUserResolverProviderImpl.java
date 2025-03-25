package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;

@Component(service = BlackbirdServiceUserResolverProvider.class, immediate = true)
public class BlackbirdServiceUserResolverProviderImpl implements BlackbirdServiceUserResolverProvider {

    private static final String CONTENT_STRUCTURE_READER_SERVICE_NAME = "blackbird-content-structure-reader-service";
    private static final String UPDATES_READER_SERVICE_NAME = "blackbird-updates-reader-service";
    private static final String PAGE_CONTENT_READER_SERVICE_NAME = "blackbird-page-content-reader-service";
    private static final String TRANSLATION_WRITER_SERVICE_NAME = "blackbird-translation-writer-service";

    private static final Map<String, Object> CONTENT_STRUCTURE_READER_PARAMS = Map.of(ResourceResolverFactory.SUBSERVICE, CONTENT_STRUCTURE_READER_SERVICE_NAME);
    private static final Map<String, Object> UPDATES_READER_PARAMS = Map.of(ResourceResolverFactory.SUBSERVICE, UPDATES_READER_SERVICE_NAME);
    private static final Map<String, Object> PAGE_CONTENT_READER_PARAMS = Map.of(ResourceResolverFactory.SUBSERVICE, PAGE_CONTENT_READER_SERVICE_NAME);
    private static final Map<String, Object> TRANSLATION_WRITER_PARAMS = Map.of(ResourceResolverFactory.SUBSERVICE, TRANSLATION_WRITER_SERVICE_NAME);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public ResourceResolver getContentStructureReaderResolver() throws LoginException {
        return resourceResolverFactory.getServiceResourceResolver(CONTENT_STRUCTURE_READER_PARAMS);
    }

    @Override
    public ResourceResolver getUpdatesReaderResolver() throws LoginException {
        return resourceResolverFactory.getServiceResourceResolver(UPDATES_READER_PARAMS);
    }

    @Override
    public ResourceResolver getPageContentReaderResolver() throws LoginException {
        return resourceResolverFactory.getServiceResourceResolver(PAGE_CONTENT_READER_PARAMS);
    }

    @Override
    public ResourceResolver getTranslationWriterResolver() throws LoginException {
        return resourceResolverFactory.getServiceResourceResolver(TRANSLATION_WRITER_PARAMS);
    }

}
