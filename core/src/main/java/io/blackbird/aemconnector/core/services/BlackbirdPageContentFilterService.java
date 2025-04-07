package io.blackbird.aemconnector.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Set;

public interface BlackbirdPageContentFilterService {

    Set<String> getBlacklistedPropertyNames();

    Set<String> getBlacklistedNodeNames();

    ObjectNode filterContent(String json);

    ObjectNode filterContent(JsonNode node);

}
