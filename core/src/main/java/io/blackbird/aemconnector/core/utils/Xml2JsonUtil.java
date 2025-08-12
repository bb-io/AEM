package io.blackbird.aemconnector.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

@Slf4j
public final class Xml2JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String INDEX_SEPARATOR = "_";
    private static final String NODE_TEXT_FIELD_NAME = "__text";
    private static final String TEXT_PLACEHOLDER_SEPARATOR = "%";

    private Xml2JsonUtil() {
    }

    public static ObjectNode convert(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder documentBuilder = configureDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        Element rootElement = document.getDocumentElement();
        ObjectNode rootJson = MAPPER.createObjectNode();
        ObjectNode elementJson = processElement(rootElement);
        rootJson.set(rootElement.getNodeName() + "_1", elementJson);
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
        StringBuilder directTextContent = new StringBuilder();
        StringBuilder fullTextContent = new StringBuilder();

        processAttributes(element, jsonObject);

        Map<String, Integer> elementCounters = new HashMap<>();
        processChildNodesInOrder(element, jsonObject, directTextContent, fullTextContent, elementCounters);
        handleTextContent(
                directTextContent,
                fullTextContent,
                jsonObject.size() == getAttributeCount(element),
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

    private static int getAttributeCount(Element element) {
        return element.getAttributes().getLength();
    }

    private static void processChildNodesInOrder(
            Element element,
            ObjectNode jsonObject,
            StringBuilder directTextContent,
            StringBuilder fullTextContent,
            Map<String, Integer> elementCounters) {
        NodeList childNodes = element.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) node;
                String tagName = childElement.getNodeName();
                int currentCount = elementCounters.getOrDefault(tagName, 0) + 1;
                elementCounters.put(tagName, currentCount);

                String indexedTagName = tagName + INDEX_SEPARATOR + currentCount;
                String indexedPlaceholder = TEXT_PLACEHOLDER_SEPARATOR + indexedTagName + TEXT_PLACEHOLDER_SEPARATOR;
                jsonObject.set(indexedTagName, processElement(childElement));
                fullTextContent.append(indexedPlaceholder);
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                String textValue = node.getNodeValue();

                directTextContent.append(textValue);
                fullTextContent.append(textValue);
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
