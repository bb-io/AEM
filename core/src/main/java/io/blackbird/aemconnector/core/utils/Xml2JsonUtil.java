package io.blackbird.aemconnector.core.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Xml2JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String NODE_TEXT_FIELD_NAME = "__text";
    private static final String TEXT_PLACEHOLDER_SEPARATOR = "%";

    private Xml2JsonUtil() {
    }

    public static ObjectNode convert(InputStream inputStream) throws Exception {
        DocumentBuilder documentBuilder = configureDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        Element rootElement = document.getDocumentElement();
        ObjectNode rootJson = MAPPER.createObjectNode();
        ObjectNode elementJson = processElement(rootElement);
        rootJson.set(rootElement.getNodeName(), elementJson);
        return rootJson;
    }

    private static DocumentBuilder configureDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory.newDocumentBuilder();
    }

    private static ObjectNode processElement(Element element) {
        ObjectNode jsonObject = MAPPER.createObjectNode();
        Map<String, List<Element>> childGroups = new HashMap<>();
        StringBuilder directTextContent = new StringBuilder();
        StringBuilder fullTextContent = new StringBuilder();

        processAttributes(element, jsonObject);
        collectChildNodesAndText(element, childGroups, directTextContent, fullTextContent);
        processChildElements(childGroups, jsonObject);
        handleTextContent(
                directTextContent,
                fullTextContent,
                childGroups.isEmpty(),
                jsonObject);

        return jsonObject;
    }

    private static void processAttributes(Element element, ObjectNode jsonObject) {
        NamedNodeMap attributes = element.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String attributeName = "_" + attribute.getNodeName();
            String attributeValue = attribute.getNodeValue();

            jsonObject.put(attributeName, attributeValue);
        }
    }

    private static void collectChildNodesAndText(
            Element element,
            Map<String, List<Element>> childGroups,
            StringBuilder directTextContent,
            StringBuilder fullTextContent) {
        NodeList childNodes = element.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) node;
                String tagName = childElement.getNodeName();

                childGroups.computeIfAbsent(tagName, k -> new ArrayList<>())
                        .add(childElement);
                fullTextContent.append(TEXT_PLACEHOLDER_SEPARATOR).append(tagName).append(TEXT_PLACEHOLDER_SEPARATOR);
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                String textValue = node.getNodeValue();

                directTextContent.append(textValue);
                fullTextContent.append(textValue);
            }
        }
    }

    private static void processChildElements(
            Map<String, List<Element>> childGroups,
            ObjectNode jsonObject) {
        for (Map.Entry<String, List<Element>> entry : childGroups.entrySet()) {
            String tagName = entry.getKey();
            List<Element> elements = entry.getValue();

            if (elements.size() == 1) {
                jsonObject.set(tagName, processElement(elements.get(0)));
            } else {
                ArrayNode arrayNode = MAPPER.createArrayNode();

                for (Element childElement : elements) {
                    arrayNode.add(processElement(childElement));
                }

                jsonObject.set(tagName, arrayNode);
            }
        }
    }

    private static void handleTextContent(
            StringBuilder directTextContent,
            StringBuilder fullTextContent,
            boolean hasNoChildElements,
            ObjectNode jsonObject) {
        String cleanDirectText = normalizeWhitespace(directTextContent.toString());

        if (!cleanDirectText.isEmpty()) {
            if (hasNoChildElements) {
                jsonObject.put(NODE_TEXT_FIELD_NAME, cleanDirectText);
            } else {
                String finalText = normalizeWhitespace(fullTextContent.toString());
                jsonObject.put(NODE_TEXT_FIELD_NAME, finalText);
            }
        }
    }

    private static String normalizeWhitespace(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }
}
