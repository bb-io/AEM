package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.models.configs.PageContentFilterConfig;

public interface BlackbirdConnectorConfigurationService {

    String CONFIG_PATH_DEFAULT = "/conf/bb-aem-connector/settings/cloudconfigs";

    PageContentFilterConfig getPageContentFilterConfig();

}
