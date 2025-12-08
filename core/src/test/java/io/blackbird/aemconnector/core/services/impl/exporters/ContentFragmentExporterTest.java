package io.blackbird.aemconnector.core.services.impl.exporters;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentVariation;
import com.adobe.cq.dam.cfm.DataType;
import com.adobe.cq.dam.cfm.ElementTemplate;
import com.adobe.cq.dam.cfm.FragmentData;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.dto.v2.ContentReference;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class ContentFragmentExporterTest {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private Resource resource;
    @Mock
    private ContentFragment contentFragment;
    @Mock
    private FragmentTemplate template;
    @Mock
    private TranslationRulesService translationRulesService;

    private ContentFragmentExporter target;

    @BeforeEach
    void setUp() {

        target = new ContentFragmentExporter(translationRulesService);
    }

    @Test
    void testCanExport() {
        assertTrue(target.canExport(ContentType.CONTENT_FRAGMENT));
        assertFalse(target.canExport(ContentType.UNKNOWN));
    }

    @Test
    void testContentFragmentExport() throws JsonProcessingException {
        Iterator<ElementTemplate> dummyElementTemplates = getDummyElementTemplates();
        Iterator<ContentElement> dummyContentElements = getDummyContentElements();

        when(resource.adaptTo(ContentFragment.class)).thenReturn(contentFragment);
        when(template.getElements()).thenReturn(dummyElementTemplates);
        when(contentFragment.getTemplate()).thenReturn(template);

        when(contentFragment.getElements()).thenReturn(dummyContentElements);

        ObjectNode result = (ObjectNode) target.export(resource, Collections.emptyMap());

        JsonNode references = result.path("references");
        ContentReference[] contentReferences = MAPPER.treeToValue(references, ContentReference[].class);

        assertTrue(contentReferences.length > 0);
        ContentReference contentReference = contentReferences[0];

        assertEquals("authorReference", contentReference.getPropertyName());
        assertEquals("jcr:content/data/master", contentReference.getPropertyPath());
        assertEquals("/content/dam/author", contentReference.getReferencePath());
        assertEquals("Test title", result.at("/jcr:content/data/master/title").asText());
        assertEquals("test description", result.at("/jcr:content/data/master/description").asText());

    }

    private Iterator<ContentElement> getDummyContentElements() {
        ContentElement element1 = mockContentElement("title", "Test title", "string");
        ContentElement element2 = mockContentElement("description", "test description", "string");
        ContentElement element3 = mockContentElement("authorReference", "/content/dam/author", "content-fragment");
        return Arrays.asList(element1, element2, element3).iterator();
    }

    private ContentElement mockContentElement(String name, String value, String type) {
        ContentElement element = Mockito.mock(ContentElement.class);
        FragmentData fragmentData = Mockito.mock(FragmentData.class);
        DataType dataType = Mockito.mock(DataType.class);
        ContentVariation contentVariation = Mockito.mock(ContentVariation.class);
        Mockito.lenient().when(element.getName()).thenReturn(name);
        Mockito.lenient().when(element.getValue()).thenReturn(fragmentData);
        Mockito.lenient().when(element.getVariation(any(String.class))).thenReturn(contentVariation);
        Mockito.lenient().when(fragmentData.getValue()).thenReturn(value);
        Mockito.lenient().when(fragmentData.getDataType()).thenReturn(dataType);
        Mockito.lenient().when(dataType.getSemanticType()).thenReturn(type);

        return element;
    }

    private Iterator<ElementTemplate> getDummyElementTemplates() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("translatable", Boolean.TRUE);
        ElementTemplate element1 = mockElementTemplate("title", metadata);
        ElementTemplate element2 = mockElementTemplate("description", metadata);
        Map<String, Object> metadata3 = new HashMap<>();
        ElementTemplate element3 = mockElementTemplate("authorReference", metadata3);

        return Arrays.asList(element1, element2, element3).iterator();
    }

    private ElementTemplate mockElementTemplate(String name, Map<String, Object> metadata) {
        ElementTemplate element = Mockito.mock(ElementTemplate.class);
        when(element.getName()).thenReturn(name);
        when(element.getMetaData()).thenReturn(metadata);
        return element;
    }
}