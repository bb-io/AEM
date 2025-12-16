package io.blackbird.aemconnector.core.utils;

import com.day.cq.wcm.api.constants.NameConstants;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.math.BigDecimal;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class Node2JsonUtilTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    @Mock
    private TranslationRulesService translationRulesService;

    @BeforeEach
    void setUp() throws BlackbirdInternalErrorException {
        Mockito.lenient().when(translationRulesService.isTranslatable(any(Property.class))).thenReturn(true);
        Mockito.lenient().when(translationRulesService.isTranslatable(any(Node.class))).thenReturn(TranslationRulesService.IsNodeTranslatable.TRANSLATABLE);

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

        context.create().resource("/content/test/page",
                JcrConstants.JCR_PRIMARYTYPE, NameConstants.NT_PAGE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.JANUARY, 1, 12, 0, 0);

        context.create().resource("/content/test/additional-types",
                "decimal", new BigDecimal("123.456"),
                "date", calendar);

        context.create().resource("/content/test-without-inheritance",
                "text", "Test text");
    }

    @Test
    void testSerializeSimpleProperties() throws BlackbirdInternalErrorException {
        Node node = context.resourceResolver().getResource("/content/test").adaptTo(Node.class);
        assertNotNull(node);
        ObjectNode json = Node2JsonUtil.serializeRecursively(node, translationRulesService, false);

        assertEquals("My Test Page", json.get("jcr:title").asText());
        assertTrue(json.get("isPublished").asBoolean());
        assertEquals(123, json.get("views").asInt());
        assertEquals(19.99, json.get("price").asDouble());
    }

    @Test
    void testSerializeStringArray() throws BlackbirdInternalErrorException {
        Node node = context.resourceResolver().getResource("/content/test").adaptTo(Node.class);
        assertNotNull(node);
        ObjectNode json = Node2JsonUtil.serializeRecursively(node, translationRulesService, false);

        assertTrue(json.has("tags"));
        assertEquals("aem", json.get("tags").get(0).asText());
        assertEquals("json", json.get("tags").get(1).asText());
    }

    @Test
    void testSerializeDoubleArray() throws BlackbirdInternalErrorException {
        Node node = context.resourceResolver().getResource("/content/test").adaptTo(Node.class);
        assertNotNull(node);
        ObjectNode json = Node2JsonUtil.serializeRecursively(node, translationRulesService, false);

        assertTrue(json.has("ratings"));
        assertEquals(4.5, json.get("ratings").get(0).asDouble());
        assertEquals(4.8, json.get("ratings").get(1).asDouble());
    }

    @Test
    void testSerializeNestedChild() throws BlackbirdInternalErrorException {
        Node node = context.resourceResolver().getResource("/content/test").adaptTo(Node.class);
        assertNotNull(node);
        ObjectNode json = Node2JsonUtil.serializeRecursively(node, translationRulesService, false);

        assertTrue(json.has("child"));
        assertEquals("Child Node", json.get("child").get("text").asText());
        assertFalse(json.get("child").get("enabled").asBoolean());
    }

    @Test
    void testNullTranslationRulesService() {
        Node node = context.resourceResolver().getResource("/content/test").adaptTo(Node.class);
        assertNotNull(node);

        BlackbirdInternalErrorException exception = assertThrows(BlackbirdInternalErrorException.class, () -> {
            Node2JsonUtil.serializeRecursively(node, null, false);
        });

        assertEquals("TranslationRulesService is null", exception.getMessage());
    }

    @Test
    void testNullNode() throws BlackbirdInternalErrorException {
        ObjectNode json = Node2JsonUtil.serializeRecursively(null, translationRulesService, false);
        assertNotNull(json);
        assertEquals(0, json.size());
    }

    @Test
    void testNonTranslatableProperty() throws BlackbirdInternalErrorException, RepositoryException {
        Node node = context.resourceResolver().getResource("/content/test").adaptTo(Node.class);
        assertNotNull(node);
        Property titleProperty = node.getProperty("jcr:title");
        when(translationRulesService.isTranslatable(titleProperty)).thenReturn(false);

        ObjectNode json = Node2JsonUtil.serializeRecursively(node, translationRulesService, false);

        assertFalse(json.has("jcr:title"));
    }

    @Test
    void testOnlyChildrenTranslatable() throws BlackbirdInternalErrorException {
        Node node = context.resourceResolver().getResource("/content/test").adaptTo(Node.class);
        assertNotNull(node);
        when(translationRulesService.isTranslatable(node))
            .thenReturn(TranslationRulesService.IsNodeTranslatable.ONLY_CHILDREN_TRANSLATABLE);

        ObjectNode json = Node2JsonUtil.serializeRecursively(node, translationRulesService, false);

        assertFalse(json.has("jcr:title"));
        assertTrue(json.has("child"));
    }

    @Test
    void testAdditionalPropertyTypes() throws BlackbirdInternalErrorException {
        Node node = context.resourceResolver().getResource("/content/test/additional-types").adaptTo(Node.class);
        assertNotNull(node);

        ObjectNode json = Node2JsonUtil.serializeRecursively(node, translationRulesService, false);

        assertTrue(json.has("decimal"));
        assertEquals(123.456, json.get("decimal").asDouble(), 0.001);
        assertTrue(json.has("date"));
        assertNotNull(json.get("date").asText());
    }

    @Test
    void testIsNotPageNode() throws BlackbirdInternalErrorException, RepositoryException {
        Node regularNode = context.resourceResolver().getResource("/content/test").adaptTo(Node.class);
        assertNotNull(regularNode);
        Node pageNode = context.resourceResolver().getResource("/content/test/page").adaptTo(Node.class);
        assertNotNull(pageNode);
        Node mockPageNode = Mockito.mock(Node.class);
        PropertyIterator mockPropertyIterator = Mockito.mock(PropertyIterator.class);
        NodeIterator mockNodeIterator = Mockito.mock(NodeIterator.class);
        when(mockPageNode.getProperties()).thenReturn(mockPropertyIterator);
        when(mockPropertyIterator.hasNext()).thenReturn(false);
        when(mockPageNode.getNodes()).thenReturn(mockNodeIterator);
        when(mockNodeIterator.hasNext()).thenReturn(false);

        ObjectNode pageJson = Node2JsonUtil.serializeRecursively(mockPageNode, translationRulesService, false);
        ObjectNode regularJson = Node2JsonUtil.serializeRecursively(regularNode, translationRulesService, false);

        assertTrue(regularJson.has("child"));
        assertEquals(0, pageJson.size());
    }

    @Test
    void testRepositoryException() throws RepositoryException {
        Node mockNode = Mockito.mock(Node.class);
        when(mockNode.getProperties()).thenThrow(new RepositoryException("Test exception"));

        BlackbirdInternalErrorException exception = assertThrows(BlackbirdInternalErrorException.class,
                () -> Node2JsonUtil.serializeRecursively(mockNode, translationRulesService, false));

        assertEquals("Error accessing JCR node: Test exception", exception.getMessage());
    }

    @Test
    void testCancelInheritance() throws BlackbirdInternalErrorException, RepositoryException {
        Node node = context.resourceResolver().getResource("/content/test-without-inheritance").adaptTo(Node.class);
        assertNotNull(node);
        Property textProperty = node.getProperty("text");
        when(translationRulesService.isTranslatable(textProperty)).thenReturn(true);

        ObjectNode json = Node2JsonUtil.serializeRecursively(node, translationRulesService, true);

        assertTrue(json.has("text"));
    }

}
