package io.blackbird.aemconnector.core.services.impl.rules;

import io.blackbird.aemconnector.core.dto.TranslationRules;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Parser for translation rules XML files.
 */
@Slf4j
public class TranslationRulesFileParser {

    private static final String AN_ASSET_REFERENCE_ATTRIBUTE = "assetReferenceAttribute";
    private static final String AN_CHECK_IN_CHILD_NODES = "checkInChildNodes";
    private static final String AN_CONTAINS_PROPERTY = "containsProperty";
    private static final String AN_CREATE_LANG_COPY = "createLangCopy";
    private static final String AN_INHERIT = "inherit";
    private static final String AN_IS_DEEP = "isDeep";
    private static final String AN_NAME = "name";
    private static final String AN_PATH = "path";
    private static final String AN_PATH_CONTAINS = "pathContains";
    private static final String AN_PROPERTY_VALUE = "propertyValue";
    private static final String AN_RESOURCE_TYPE = "resourceType";
    private static final String AN_TRANSLATE = "translate";
    private static final String AN_UPDATE_DESTINATION_LANGUAGE = "updateDestinationLanguage";
    private static final String NN_ASSET_NODE = "assetNode";
    private static final String NN_FILTER = "filter";
    private static final String NN_NODE = "node";
    private static final String NN_PROPERTY = "property";

    private static final BiPredicate<Node, String> HAS_ATTRIBUTE =
            (node, attributeName) -> null != node.getAttributes().getNamedItem(attributeName);
    private static final Predicate<Node> IS_CONTEXT_RULE =
            node -> NN_NODE.equals(node.getNodeName()) && HAS_ATTRIBUTE.test(node, AN_PATH);
    private static final Predicate<Node> IS_NODE_FILTER_RULE =
            node -> NN_FILTER.equals(node.getNodeName());
    private static final Predicate<Node> IS_PROPERTY_FILTER_RULE =
            node -> NN_NODE.equals(node.getNodeName()) && HAS_ATTRIBUTE.test(node, AN_PATH_CONTAINS);
    private static final Predicate<Node> IS_RESOURCE_TYPE_RULE =
            node -> NN_NODE.equals(node.getNodeName()) && HAS_ATTRIBUTE.test(node, AN_RESOURCE_TYPE);
    private static final Predicate<Node> IS_GENERAL_RULE =
            node -> NN_PROPERTY.equals(node.getNodeName()) && HAS_ATTRIBUTE.test(node, AN_NAME);
    private static final Predicate<Node> IS_ASSET_NODE =
            node -> NN_ASSET_NODE.equals(node.getNodeName())
                    && HAS_ATTRIBUTE.test(node, AN_ASSET_REFERENCE_ATTRIBUTE)
                    && HAS_ATTRIBUTE.test(node, AN_RESOURCE_TYPE)
                    && HAS_ATTRIBUTE.test(node, AN_CHECK_IN_CHILD_NODES)
                    && HAS_ATTRIBUTE.test(node, AN_CREATE_LANG_COPY);
    private static final Predicate<Node> UNRECOGNIZED_RULE = node -> true;

    private final Map<Predicate<Node>, Consumer<Node>> ruleParserMap = new LinkedHashMap<>();
    private final List<AssetReferenceRule> assetReferenceRules = new ArrayList<>();
    private List<TranslationNodeFilterRule> translationNodeFilterRules = new ArrayList<>();
    private List<TranslationPropertyRule> propertyFilterRules = new ArrayList<>();
    private List<TranslationPropertyRule> resourceTypeRules = new ArrayList<>();
    private List<TranslationPropertyRule> generalRules = new ArrayList<>();

    {
        ruleParserMap.put(IS_NODE_FILTER_RULE, n -> addNonNullRule(parseNodeFilterRule(n), translationNodeFilterRules));
        ruleParserMap.put(IS_PROPERTY_FILTER_RULE, n -> addNonNullRule(parsePropertyFilterRule(n), propertyFilterRules));
        ruleParserMap.put(IS_RESOURCE_TYPE_RULE, n -> addNonNullRule(parseResourceTypeRule(n), resourceTypeRules));
        ruleParserMap.put(IS_GENERAL_RULE, n -> addNonNullRule(parseGeneralRule(n), generalRules));
        ruleParserMap.put(UNRECOGNIZED_RULE, n -> log.trace("Unrecognized rule node: {}", nodeAsString(n)));
    }

    /**
     * Parses an XML input stream into a list of ContextRule objects.
     *
     * @param inputStream The XML input stream to parse
     * @return A list of ContextRule objects
     */
    public TranslationRules parse(InputStream inputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            NodeList nodes = document.getDocumentElement().getChildNodes();
            List<ContextRule> contextRules = IntStream.range(0, nodes.getLength())
                    .mapToObj(nodes::item)
                    .map(this::parseRuleNode)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(ContextRule::getContextPath).reversed())
                    .collect(Collectors.toList());
            return new TranslationRules(contextRules, assetReferenceRules);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("Error parsing translation rules XML", e);
        }

        return TranslationRules.EMPTY;
    }

    private ContextRule parseRuleNode(Node node) {
        if (IS_CONTEXT_RULE.test(node)) {
            return parseContextRule(node);
        } else if (IS_ASSET_NODE.test(node)) {
            addNonNullRule(parseAssetReferenceRule(node), assetReferenceRules);
        }
        return null;
    }

    private ContextRule parseContextRule(Node node) {
        translationNodeFilterRules = new ArrayList<>();
        propertyFilterRules = new ArrayList<>();
        resourceTypeRules = new ArrayList<>();
        generalRules = new ArrayList<>();

        NodeList ruleNodes = node.getChildNodes();
        IntStream.range(0, ruleNodes.getLength())
                .mapToObj(ruleNodes::item)
                .forEach(n -> ruleParserMap.entrySet().stream()
                        .filter(e -> e.getKey().test(n)).findFirst()
                        .ifPresent(e -> e.getValue().accept(n)));

        if (translationNodeFilterRules.isEmpty()
                && propertyFilterRules.isEmpty()
                && resourceTypeRules.isEmpty()
                && generalRules.isEmpty()) {
            log.trace("No translation rules found for context path: {}. Context skipped.", getAttribute(node, AN_PATH));
            return null;
        }

        return ContextRule.builder()
                .contextPath(getAttribute(node, AN_PATH))
                .translationNodeFilterRules(translationNodeFilterRules)
                .propertyFilterRules(propertyFilterRules)
                .resourceTypeRules(resourceTypeRules)
                .generalRules(generalRules)
                .build();
    }

    private TranslationNodeFilterRule parseNodeFilterRule(Node node) {
        NodeList propertyNodes = node.getChildNodes();

        Node ruleNode = IntStream.range(0, propertyNodes.getLength())
                .mapToObj(propertyNodes::item)
                .filter(n -> NN_NODE.equals(n.getNodeName()))
                .findFirst()
                .orElse(null);

        if (null != ruleNode
                && HAS_ATTRIBUTE.test(ruleNode, AN_CONTAINS_PROPERTY)
                && HAS_ATTRIBUTE.test(ruleNode, AN_PROPERTY_VALUE)) {

            return TranslationNodeFilterRule.builder()
                    .propertyName(getAttribute(ruleNode, AN_CONTAINS_PROPERTY))
                    .propertyValue(getAttribute(ruleNode, AN_PROPERTY_VALUE))
                    .isDeep(getAttributeOrDefault(ruleNode, AN_IS_DEEP, false))
                    .build();
        } else {
            log.trace("Failed to parse node filter rule: {}", nodeAsString(node));
            return null;
        }
    }

    private TranslationPropertyRule parsePropertyFilterRule(Node node) {
        String pathContains = getAttribute(node, AN_PATH_CONTAINS);

        NodeList propertyNodes = node.getChildNodes();
        String propertyName = IntStream.range(0, propertyNodes.getLength())
                .mapToObj(propertyNodes::item)
                .filter(IS_GENERAL_RULE)
                .findFirst()
                .map(n -> getAttribute(n, AN_NAME))
                .orElse(null);

        if (null != propertyName) {
            return PropertyFilterRule.builder()
                    .pathContains(pathContains)
                    .propertyName(propertyName)
                    .build();
        } else {
            log.trace("Failed to parse property filter rule: {}", nodeAsString(node));
            return null;
        }
    }

    private TranslationPropertyRule parseResourceTypeRule(Node node) {
        String resourceType = getAttribute(node, AN_RESOURCE_TYPE);

        NodeList propertyNodes = node.getChildNodes();
        List<TranslationPropertyRule> propertyNameRules = IntStream.range(0, propertyNodes.getLength())
                .mapToObj(propertyNodes::item)
                .filter(IS_GENERAL_RULE)
                .map(this::parseGeneralRule)
                .collect(Collectors.toList());

        if (!propertyNameRules.isEmpty()) {
            return ResourceTypeRule.builder()
                    .resourceType(resourceType)
                    .propertyNameRules(propertyNameRules)
                    .build();
        } else {
            log.trace("Failed to parse resourceType rule: {}", nodeAsString(node));
            return null;
        }
    }

    private TranslationPropertyRule parseGeneralRule(Node node) {
        return GeneralRule.builder()
                .name(getAttribute(node, AN_NAME))
                .translate(getAttributeOrDefault(node, AN_TRANSLATE, true))
                .inherit(getAttributeOrDefault(node, AN_INHERIT, false))
                .updateDestinationLanguage(getAttributeOrDefault(node, AN_UPDATE_DESTINATION_LANGUAGE, false))
                .build();
    }

    private AssetReferenceRule parseAssetReferenceRule(Node node) {
        return AssetReferenceRule.builder()
                .assetReferenceAttribute(getAttribute(node, AN_ASSET_REFERENCE_ATTRIBUTE))
                .resourceType(getAttribute(node, AN_RESOURCE_TYPE))
                .checkInChildNodes(Boolean.parseBoolean(getAttribute(node, AN_CHECK_IN_CHILD_NODES)))
                .createLangCopy(Boolean.parseBoolean(getAttribute(node, AN_CREATE_LANG_COPY)))
                .build();
    }

    private String getAttribute(Node node, String attributeName) {
        return node.getAttributes().getNamedItem(attributeName).getNodeValue();
    }

    private boolean getAttributeOrDefault(Node node, String attributeName, boolean defaultValue) {
        return HAS_ATTRIBUTE.test(node, attributeName)
                ? Boolean.parseBoolean(getAttribute(node, attributeName))
                : defaultValue;
    }

    private <T> void addNonNullRule(T translationRule, List<T> rulesList) {
        if (null != translationRule) {
            rulesList.add(translationRule);
        }
    }

    private String nodeAsString(Node node) {
        StringWriter writer = new StringWriter();
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            transformer.transform(new DOMSource(node), new StreamResult(writer));
        } catch (TransformerException e) {
            log.trace("Failed to write XML node={} to string", node);
        }
        return writer.toString();
    }

}
