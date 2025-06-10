package io.blackbird.aemconnector.core.utils.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

public class JsonUtils {

    private JsonUtils() {
    }

    public static void removeEmptyObjects(ObjectNode node) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode child = entry.getValue();
            if (child.isObject()) {
                removeEmptyObjects((ObjectNode) child);
                if (child.isEmpty()) {
                    fields.remove();
                }
            }
        }
    }
}
