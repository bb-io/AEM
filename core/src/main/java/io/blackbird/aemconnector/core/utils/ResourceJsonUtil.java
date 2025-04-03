package io.blackbird.aemconnector.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.math.BigDecimal;
import java.util.Map;

public final class ResourceJsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    private ResourceJsonUtil() {
    }

    public static ObjectNode serializeRecursively(Resource resource) {
        ObjectNode node = mapper.createObjectNode();

        ValueMap props = resource.getValueMap();

        for (Map.Entry<String, Object> entry : props.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                node.put(key, (String) value);
            } else if (value instanceof Boolean) {
                node.put(key, (Boolean) value);
            } else if (value instanceof Integer) {
                node.put(key, (Integer) value);
            } else if (value instanceof Long) {
                node.put(key, (Long) value);
            } else if (value instanceof BigDecimal) {
                node.put(key, (BigDecimal) value); }
            else if (value instanceof Double) {
                node.put(key, (Double) value);
            } else if (value instanceof Object[]) {
                node.set(key, serializeObjectArray((Object[]) value));
            } else {
                node.put(key, String.valueOf(value));
            }
        }

        for (Resource child : resource.getChildren()) {
            node.set(child.getName(), serializeRecursively(child));
        }

        return node;
    }

    private static ArrayNode serializeObjectArray(Object[] array) {
        ArrayNode jsonArray = mapper.createArrayNode();

        for (Object item : array) {
            if (item instanceof String) {
                jsonArray.add((String) item);
            } else if (item instanceof Integer) {
                jsonArray.add((Integer) item);
            } else if (item instanceof Long) {
                jsonArray.add((Long) item);
            } else if (item instanceof Double) {
                jsonArray.add((Double) item);
            } else if (item instanceof Boolean) {
                jsonArray.add((Boolean) item);
            } else if (item instanceof BigDecimal) {
                jsonArray.add((BigDecimal) item);
            } else {
                jsonArray.add(String.valueOf(item));
            }
        }

        return jsonArray;
    }
}
