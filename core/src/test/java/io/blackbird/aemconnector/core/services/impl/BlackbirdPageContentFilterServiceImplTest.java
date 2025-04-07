package io.blackbird.aemconnector.core.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.models.configs.PageContentFilterConfig;
import io.blackbird.aemconnector.core.services.BlackbirdConnectorConfigurationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class BlackbirdPageContentFilterServiceImplTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private BlackbirdConnectorConfigurationService configurationService;

    @Mock
    private PageContentFilterConfig pageContentFilterConfig;

    @Mock
    private PageContentFilterConfig.Property property1;

    @Mock
    private PageContentFilterConfig.Property property2;

    @InjectMocks
    private BlackbirdPageContentFilterServiceImpl target;

    @BeforeEach
    void setUp() {
        // Common setup for tests if needed
    }

    @Test
    void shouldReturnBlacklistedPropertyNames() {
        // GIVEN
        List<PageContentFilterConfig.Property> properties = Arrays.asList(property1, property2);
        Mockito.when(configurationService.getPageContentFilterConfig()).thenReturn(pageContentFilterConfig);
        Mockito.when(pageContentFilterConfig.getBlacklistedPropertyNames()).thenReturn(properties);
        Mockito.when(property1.getPropertyName()).thenReturn("prop1");
        Mockito.when(property2.getPropertyName()).thenReturn("prop2");

        // WHEN
        Set<String> result = target.getBlacklistedPropertyNames();

        // THEN
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains("prop1"));
        Assertions.assertTrue(result.contains("prop2"));
    }

    @Test
    void shouldReturnEmptySetWhenNoBlacklistedPropertyNames() {
        // GIVEN
        Mockito.when(configurationService.getPageContentFilterConfig()).thenReturn(pageContentFilterConfig);
        Mockito.when(pageContentFilterConfig.getBlacklistedPropertyNames()).thenReturn(Collections.emptyList());

        // WHEN
        Set<String> result = target.getBlacklistedPropertyNames();

        // THEN
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnBlacklistedNodeNames() {
        // GIVEN
        List<PageContentFilterConfig.Property> properties = Arrays.asList(property1, property2);
        Mockito.when(configurationService.getPageContentFilterConfig()).thenReturn(pageContentFilterConfig);
        Mockito.when(pageContentFilterConfig.getBlacklistedNodeNames()).thenReturn(properties);
        Mockito.when(property1.getPropertyName()).thenReturn("node1");
        Mockito.when(property2.getPropertyName()).thenReturn("node2");

        // WHEN
        Set<String> result = target.getBlacklistedNodeNames();

        // THEN
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains("node1"));
        Assertions.assertTrue(result.contains("node2"));
    }

    @Test
    void shouldReturnEmptySetWhenNoBlacklistedNodeNames() {
        // GIVEN
        Mockito.when(configurationService.getPageContentFilterConfig()).thenReturn(pageContentFilterConfig);
        Mockito.when(pageContentFilterConfig.getBlacklistedNodeNames()).thenReturn(Collections.emptyList());

        // WHEN
        Set<String> result = target.getBlacklistedNodeNames();

        // THEN
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldFilterContentFromJsonString() {
        // GIVEN
        String json = "{\"prop1\":\"value1\",\"prop2\":\"value2\",\"node1\":{\"subprop\":\"subvalue\"}}";
        Mockito.when(configurationService.getPageContentFilterConfig()).thenReturn(pageContentFilterConfig);
        Mockito.when(pageContentFilterConfig.getBlacklistedPropertyNames()).thenReturn(Collections.singletonList(property1));
        Mockito.when(pageContentFilterConfig.getBlacklistedNodeNames()).thenReturn(Collections.singletonList(property2));
        Mockito.when(property1.getPropertyName()).thenReturn("prop1");
        Mockito.when(property2.getPropertyName()).thenReturn("node1");

        // WHEN
        ObjectNode result = target.filterContent(json);

        // THEN
        Assertions.assertFalse(result.has("prop1"));
        Assertions.assertTrue(result.has("prop2"));
        Assertions.assertFalse(result.has("node1"));
    }

    @Test
    void shouldReturnEmptyObjectNodeForNullOrEmptyJsonString() {
        // GIVEN
        // WHEN
        ObjectNode result1 = target.filterContent((String) null);
        ObjectNode result2 = target.filterContent("");

        // THEN
        Assertions.assertEquals(0, result1.size());
        Assertions.assertEquals(0, result2.size());
    }

    @Test
    void shouldFilterContentFromJsonNode() throws Exception {
        // GIVEN
        JsonNode jsonNode = MAPPER.readTree("{\"prop1\":\"value1\",\"prop2\":\"value2\",\"node1\":{\"subprop\":\"subvalue\"}}");
        Mockito.when(configurationService.getPageContentFilterConfig()).thenReturn(pageContentFilterConfig);
        Mockito.when(pageContentFilterConfig.getBlacklistedPropertyNames()).thenReturn(Collections.singletonList(property1));
        Mockito.when(pageContentFilterConfig.getBlacklistedNodeNames()).thenReturn(Collections.singletonList(property2));
        Mockito.when(property1.getPropertyName()).thenReturn("prop1");
        Mockito.when(property2.getPropertyName()).thenReturn("node1");

        // WHEN
        ObjectNode result = target.filterContent(jsonNode);

        // THEN
        Assertions.assertFalse(result.has("prop1"));
        Assertions.assertTrue(result.has("prop2"));
        Assertions.assertFalse(result.has("node1"));
    }

    @Test
    void shouldReturnEmptyObjectNodeForNullJsonNode() {
        // GIVEN
        // WHEN
        ObjectNode result = target.filterContent((JsonNode) null);

        // THEN
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void shouldHandleNestedObjects() throws Exception {
        // GIVEN
        JsonNode jsonNode = MAPPER.readTree("{\"prop1\":\"value1\",\"node1\":{\"subprop1\":\"subvalue1\",\"subprop2\":\"subvalue2\"}}");
        Mockito.when(configurationService.getPageContentFilterConfig()).thenReturn(pageContentFilterConfig);
        Mockito.when(pageContentFilterConfig.getBlacklistedPropertyNames()).thenReturn(Collections.singletonList(property1));
        Mockito.when(pageContentFilterConfig.getBlacklistedNodeNames()).thenReturn(Collections.emptyList());
        Mockito.when(property1.getPropertyName()).thenReturn("subprop1");

        // WHEN
        ObjectNode result = target.filterContent(jsonNode);

        // THEN
        Assertions.assertTrue(result.has("prop1"));
        Assertions.assertTrue(result.has("node1"));
        JsonNode node1 = result.get("node1");
        Assertions.assertFalse(node1.has("subprop1"));
        Assertions.assertTrue(node1.has("subprop2"));
    }

    @Test
    void shouldHandleExceptionWhenFilteringContent() throws Exception {
        // GIVEN
        String invalidJson = "{invalid json}";

        // WHEN
        ObjectNode result = target.filterContent(invalidJson);

        // THEN
        Assertions.assertEquals(0, result.size());
    }
}
