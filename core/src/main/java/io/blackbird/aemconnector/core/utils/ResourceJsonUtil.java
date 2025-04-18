package io.blackbird.aemconnector.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Predicate;

public final class ResourceJsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    private ResourceJsonUtil() {
    }

    public static ObjectNode serializeRecursively(Resource resource) {
        return serializeRecursively(resource, (p) -> true, (n) -> true);
    }

    public static ObjectNode serializeRecursively(Resource resource, Predicate<String> propertyFilter, Predicate<String> nodeFilter) {
        ObjectNode node = mapper.createObjectNode();

        ValueMap props = resource.getValueMap();

        for (Map.Entry<String, Object> entry : props.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!propertyFilter.test(key)) {
                continue;
            }

            node.set(key, toJsonNode(value));
        }

        for (Resource child : resource.getChildren()) {
            if (!nodeFilter.test(child.getName())) {
                continue;
            }

            node.set(child.getName(), serializeRecursively(child, propertyFilter, nodeFilter));
        }

        return node;
    }

    private static JsonNode toJsonNode(Object value) {
        if (value == null) {
            return mapper.nullNode();
        }
        if (value instanceof String) {
            return mapper.getNodeFactory().textNode((String) value);
        } else if (value instanceof Boolean) {
            return mapper.getNodeFactory().booleanNode((Boolean) value);
        } else if (value instanceof Integer) {
            return mapper.getNodeFactory().numberNode((Integer) value);
        } else if (value instanceof Long) {
            return mapper.getNodeFactory().numberNode((Long) value);
        } else if (value instanceof BigDecimal) {
            return mapper.getNodeFactory().numberNode((BigDecimal) value);
        } else if (value instanceof Double) {
            return mapper.getNodeFactory().numberNode((Double) value);
        } else if (value instanceof Object[]) {
            ArrayNode arrayNode = mapper.createArrayNode();
            for (Object element : (Object[]) value) {
                arrayNode.add(toJsonNode(element));
            }
            return arrayNode;
        }
        return mapper.getNodeFactory().textNode(String.valueOf(value));
    }
}
