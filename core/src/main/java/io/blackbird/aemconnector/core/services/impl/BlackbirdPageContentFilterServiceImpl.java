package io.blackbird.aemconnector.core.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.models.configs.PageContentFilterConfig;
import io.blackbird.aemconnector.core.services.BlackbirdConnectorConfigurationService;
import io.blackbird.aemconnector.core.services.BlackbirdPageContentFilterService;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component(service = BlackbirdPageContentFilterService.class, immediate = true)
@Slf4j
public class BlackbirdPageContentFilterServiceImpl implements BlackbirdPageContentFilterService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Reference
    private BlackbirdConnectorConfigurationService configurationService;

    @Override
    public Set<String> getBlacklistedPropertyNames() {
        return Optional.of(configurationService)
                .map(BlackbirdConnectorConfigurationService::getPageContentFilterConfig)
                .map(PageContentFilterConfig::getBlacklistedPropertyNames)
                .map(p -> p.stream().map(PageContentFilterConfig.Property::getPropertyName).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    @Override
    public Set<String> getBlacklistedNodeNames() {
        return Optional.of(configurationService)
                .map(BlackbirdConnectorConfigurationService::getPageContentFilterConfig)
                .map(PageContentFilterConfig::getBlacklistedNodeNames)
                .map(p -> p.stream().map(PageContentFilterConfig.Property::getPropertyName).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    @Override
    public ObjectNode filterContent(String json) {
        if (json == null || json.isEmpty()) {
            return MAPPER.createObjectNode();
        }

        try {
            JsonNode jsonNode = MAPPER.readTree(json);
            return filterContent(jsonNode);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON", e);
            return MAPPER.createObjectNode();
        }
    }

    @Override
    public ObjectNode filterContent(JsonNode node) {
        if (node == null) {
            return MAPPER.createObjectNode();
        }

        try {
            Set<String> blacklistedNodeNames = this.getBlacklistedNodeNames();
            Set<String> blacklistedPropertyNames = this.getBlacklistedPropertyNames();
            ObjectNode copy = MAPPER.createObjectNode();

            node.fieldNames().forEachRemaining(fieldName -> {
                JsonNode fieldValue = node.get(fieldName);

                if (fieldValue.isObject()) {
                    if (!blacklistedNodeNames.contains(fieldName)) {
                        copy.set(fieldName, filterContent(fieldValue));
                    }
                } else {
                    if (!blacklistedPropertyNames.contains(fieldName)) {
                        copy.set(fieldName, fieldValue);
                    }
                }
            });

            return copy;
        } catch (Exception e) {
            log.error("Error filtering JSON", e);
            return MAPPER.createObjectNode();
        }
    }

}
