package io.blackbird.aemconnector.core.services;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;

public interface BlackbirdServiceUserResolverProvider {

    ResourceResolver getContentStructureReaderResolver() throws LoginException;

    ResourceResolver getUpdatesReaderResolver() throws LoginException;

    ResourceResolver getPageContentReaderResolver() throws LoginException;

    ResourceResolver getTranslationWriterResolver() throws LoginException;

    ResourceResolver getConfigurationResolver() throws LoginException;

}
