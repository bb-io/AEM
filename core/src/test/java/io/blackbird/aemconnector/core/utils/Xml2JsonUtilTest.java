package io.blackbird.aemconnector.core.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Xml2JsonUtilTest {

    @Test
    void shouldConvertBasicXml() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><simple>value</simple></root>";
        ObjectNode result = convertXmlToJson(xml);

        ObjectNode rootNode = (ObjectNode) result.get("root");
        ObjectNode simpleNode = (ObjectNode) rootNode.get("simple");
        assertEquals("value", simpleNode.get("__text").asText());
    }

    @Test
    void shouldConvertXmlWithAttributes() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><element attr1=\"value1\" attr2=\"value2\">content</element></root>";
        ObjectNode result = convertXmlToJson(xml);

        ObjectNode elementNode = (ObjectNode) result.get("root").get("element");
        assertEquals("value1", elementNode.get("_attr1").asText());
        assertEquals("value2", elementNode.get("_attr2").asText());
        assertEquals("content", elementNode.get("__text").asText());
    }

    @Test
    void shouldConvertXmlWithChildren() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<root><parent><child1>value1</child1><child2>value2</child2></parent></root>";
        ObjectNode result = convertXmlToJson(xml);

        ObjectNode parentNode = (ObjectNode) result.get("root").get("parent");
        assertEquals("value1", parentNode.get("child1").get("__text").asText());
        assertEquals("value2", parentNode.get("child2").get("__text").asText());
    }

    @Test
    void shouldConvertXmlWithArray() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<root><item>1</item><item>2</item><item>3</item></root>";
        ObjectNode result = convertXmlToJson(xml);

        assertTrue(result.get("root").get("item").isArray());
        assertEquals(3, result.get("root").get("item").size());
        assertEquals("1", result.get("root").get("item").get(0).get("__text").asText());
        assertEquals("2", result.get("root").get("item").get(1).get("__text").asText());
        assertEquals("3", result.get("root").get("item").get(2).get("__text").asText());
    }

    @Test
    void shouldConvertXmlWithMixedEntries() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<root>Text before<child>child content</child>Text after</root>";
        ObjectNode result = convertXmlToJson(xml);

        ObjectNode rootNode = (ObjectNode) result.get("root");
        assertEquals("Text before%child%Text after", rootNode.get("__text").asText());
        assertEquals("child content", rootNode.get("child").get("__text").asText());
    }

    @Test
    void shouldConvertXmlWithEmptyTag() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><empty></empty></root>";
        ObjectNode result = convertXmlToJson(xml);

        assertNotNull(result.get("root").get("empty"));
        assertTrue(result.get("root").get("empty").isEmpty());
    }

    @Test
    void shouldThrowExceptionForInvalidXml() {
        String invalidXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><unclosed>";
        assertThrows(Exception.class, () -> convertXmlToJson(invalidXml));
    }

    private ObjectNode convertXmlToJson(String xml) throws Exception {
        try (InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            return Xml2JsonUtil.convert(is);
        }
    }
}
