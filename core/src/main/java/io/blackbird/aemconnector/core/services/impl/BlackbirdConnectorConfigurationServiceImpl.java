package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.models.configs.BlackbirdConnectorConfiguration;
import io.blackbird.aemconnector.core.models.configs.PageContentFilterConfig;
import io.blackbird.aemconnector.core.services.BlackbirdConnectorConfigurationService;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

@Slf4j
@Component(immediate = true, service = BlackbirdConnectorConfigurationService.class)
public class BlackbirdConnectorConfigurationServiceImpl implements BlackbirdConnectorConfigurationService {

    @Reference
    private BlackbirdServiceUserResolverProvider resolverProvider;

    private Optional<BlackbirdConnectorConfiguration> getConfiguration(ResourceResolver resourceResolver) {
        Resource configResource = resourceResolver.getResource(CONFIG_PATH_DEFAULT);
        return Optional.ofNullable(configResource)
                .map(r -> r.adaptTo(BlackbirdConnectorConfiguration.class));
    }

    @Override
    public PageContentFilterConfig getPageContentFilterConfig() {
        try (ResourceResolver resourceResolver = resolverProvider.getConfigurationResolver()) {
            return getConfiguration(resourceResolver)
                    .map(BlackbirdConnectorConfiguration::getPageContentFilterConfig)
                    .orElse(null);
        } catch (LoginException e) {
            log.error("Failed to read Blackbird connector configuration, path=" + CONFIG_PATH_DEFAULT, e);
        }
        return null;
    }

}
