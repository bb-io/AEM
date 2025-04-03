package io.blackbird.aemconnector.core.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({AemContextExtension.class})
class ResourceJsonUtilTest {


    @BeforeEach
    void setUp(AemContext context) {
        context.create().resource("/content/test",
                "jcr:title", "My Test Page",
                "isPublished", true,
                "views", 123,
                "price", 19.99,
                "tags", new String[]{"aem", "json"},
                "ratings", new Double[]{4.5, 4.8});

        context.create().resource("/content/test/child",
                "text", "Child Node",
                "enabled", false);
    }

    @Test
    void testSerializeSimpleProperties(AemContext context) {
        Resource resource = context.resourceResolver().getResource("/content/test");
        assertNotNull(resource);
        ObjectNode json = ResourceJsonUtil.serializeRecursively(resource);

        assertEquals("My Test Page", json.get("jcr:title").asText());
        assertTrue(json.get("isPublished").asBoolean());
        assertEquals(123, json.get("views").asInt());
        assertEquals(19.99, json.get("price").asDouble());
    }

    @Test
    void testSerializeStringArray(AemContext context) {
        Resource resource = context.resourceResolver().getResource("/content/test");
        assertNotNull(resource);

        ObjectNode json = ResourceJsonUtil.serializeRecursively(resource);

        assertTrue(json.has("tags"));
        assertEquals("aem", json.get("tags").get(0).asText());
        assertEquals("json", json.get("tags").get(1).asText());
    }

    @Test
    void testSerializeDoubleArray(AemContext context) {
        Resource resource = context.resourceResolver().getResource("/content/test");
        assertNotNull(resource);
        ObjectNode json = ResourceJsonUtil.serializeRecursively(resource);

        assertTrue(json.has("ratings"));
        assertEquals(4.5, json.get("ratings").get(0).asDouble());
        assertEquals(4.8, json.get("ratings").get(1).asDouble());
    }

    @Test
    void testSerializeNestedChild(AemContext context) {
        Resource resource = context.resourceResolver().getResource("/content/test");
        assertNotNull(resource);
        ObjectNode json = ResourceJsonUtil.serializeRecursively(resource);

        assertTrue(json.has("child"));
        assertEquals("Child Node", json.get("child").get("text").asText());
        assertFalse(json.get("child").get("enabled").asBoolean());
    }
}