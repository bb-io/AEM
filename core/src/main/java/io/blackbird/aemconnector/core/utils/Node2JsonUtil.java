package io.blackbird.aemconnector.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import static io.blackbird.aemconnector.core.utils.PageInheritanceUtils.shouldExportProperty;
import static com.day.cq.wcm.api.NameConstants.NT_PAGE;
import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;

@Slf4j
public final class Node2JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Node2JsonUtil() {
    }

    public static ObjectNode serializeRecursively(Node jcrNode, TranslationRulesService translationRulesService, boolean isLiveCopy) throws BlackbirdInternalErrorException {
        if (null == translationRulesService) {
            log.error("TranslationRulesService is null");
            throw new BlackbirdInternalErrorException("TranslationRulesService is null");
        }

        if (null == jcrNode) {
            log.debug("Node is null, returning empty JSON object");
            return MAPPER.createObjectNode();
        }

        ObjectNode jsonNode = MAPPER.createObjectNode();

        try {
            TranslationRulesService.IsNodeTranslatable isNodeTranslatable = translationRulesService.isTranslatable(jcrNode);

            if (TranslationRulesService.IsNodeTranslatable.TRANSLATABLE.equals(isNodeTranslatable)) {
                PropertyIterator propertyIterator = jcrNode.getProperties();
                while (propertyIterator.hasNext()) {
                    Property property = propertyIterator.nextProperty();
                    String key = property.getName();

                    if (!translationRulesService.isTranslatable(property)) {
                        log.trace("Property {}/{} is not translatable, skipping", jcrNode.getPath(), key);
                        continue;
                    }
                    if (isLiveCopy && !shouldExportProperty(jcrNode, property)) {
                        log.trace("Skipping inherited property {}/{}", jcrNode.getPath(), key);
                        continue;
                    }
                    jsonNode.set(key, getPropertyAsJsonNode(property));
                }
            }

            if (TranslationRulesService.IsNodeTranslatable.TRANSLATABLE.equals(isNodeTranslatable)
                    || TranslationRulesService.IsNodeTranslatable.ONLY_CHILDREN_TRANSLATABLE.equals(isNodeTranslatable)) {
                NodeIterator nodeIterator = jcrNode.getNodes();
                if (null != nodeIterator) {
                    while (nodeIterator.hasNext()) {
                        Node childNode = nodeIterator.nextNode();
                        if (isNotPageNode(childNode)) {
                            jsonNode.set(childNode.getName(), serializeRecursively(childNode, translationRulesService, isLiveCopy));
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            throw new BlackbirdInternalErrorException("Error accessing JCR node: " + e.getMessage());
        }

        return jsonNode;
    }

    public static ObjectNode serializeRecursively(Node jcrNode, boolean isLiveCopy) throws BlackbirdInternalErrorException {
        if (null == jcrNode) {
            log.debug("Node is null, returning empty JSON object");
            return MAPPER.createObjectNode();
        }

        ObjectNode jsonNode = MAPPER.createObjectNode();

        try {
            PropertyIterator propertyIterator = jcrNode.getProperties();
            while (propertyIterator.hasNext()) {
                Property property = propertyIterator.nextProperty();
                String key = property.getName();

                if (isLiveCopy && !shouldExportProperty(jcrNode, property)) {
                    log.trace("Skipping inherited property {}/{}", jcrNode.getPath(), key);
                    continue;
                }
                jsonNode.set(key, getPropertyAsJsonNode(property));
            }

            NodeIterator nodeIterator = jcrNode.getNodes();
            if (null != nodeIterator) {
                while (nodeIterator.hasNext()) {
                    Node childNode = nodeIterator.nextNode();
                    if (isNotPageNode(childNode)) {
                        jsonNode.set(childNode.getName(), serializeRecursively(childNode, isLiveCopy));
                    }
                }
            }

        } catch (RepositoryException e) {
            throw new BlackbirdInternalErrorException("Error accessing JCR node: " + e.getMessage());
        }

        return jsonNode;
    }

    private static JsonNode getPropertyAsJsonNode(Property property) throws RepositoryException {
        if (property.isMultiple()) {
            ArrayNode arrayNode = MAPPER.createArrayNode();
            Value[] values = property.getValues();
            for (Value value : values) {
                arrayNode.add(getValueAsJsonNode(value));
            }
            return arrayNode;
        } else {
            return getValueAsJsonNode(property.getValue());
        }
    }

    private static JsonNode getValueAsJsonNode(Value value) throws RepositoryException {
        switch (value.getType()) {
            case PropertyType.BOOLEAN:
                return MAPPER.getNodeFactory().booleanNode(value.getBoolean());
            case PropertyType.LONG:
                return MAPPER.getNodeFactory().numberNode(value.getLong());
            case PropertyType.DOUBLE:
                return MAPPER.getNodeFactory().numberNode(value.getDouble());
            case PropertyType.DECIMAL:
                return MAPPER.getNodeFactory().numberNode(value.getDecimal());
            case PropertyType.DATE:
                return MAPPER.getNodeFactory().textNode(value.getDate().getTime().toString());
            case PropertyType.STRING:
            default:
                return MAPPER.getNodeFactory().textNode(value.getString());
        }
    }

    private static boolean isNotPageNode(Node node) throws RepositoryException {
        return !NT_PAGE.equals(node.getProperty(JCR_PRIMARYTYPE).getString());
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
