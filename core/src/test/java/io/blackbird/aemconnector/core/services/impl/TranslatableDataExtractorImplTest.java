package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.objects.TranslatableContent;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, AemContextExtension.class})
class TranslatableDataExtractorImplTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    @Mock
    private TranslationRulesService translationRulesService;

    @Test
    void shouldExtractTranslatableDataForAGivenNode() throws BlackbirdInternalErrorException, RepositoryException {
        String path = "/content/test";
        Map<String, String> props = new HashMap<>();
        props.put("foo", "foo value");
        props.put("bar", "bar value");
        context.create().resource(path, props);

        when(translationRulesService.isTranslatable(any(Node.class))).thenReturn(TranslationRulesService.IsNodeTranslatable.TRANSLATABLE);
        when(translationRulesService.isTranslatable(any(Property.class)))
                .thenReturn(false);
        when(translationRulesService.isTranslatable(argThat((Property property) -> {
            try {
                String name = property.getName();
                return name.equals("foo") || name.equals("bar");
            } catch (RepositoryException e) {
                return false;
            }

        }))).thenReturn(true);

        Resource resource = context.resourceResolver().getResource(path);

        Node node = resource.adaptTo(Node.class);
        assertNotNull(node);

        TranslatableDataExtractorImpl dataExtractor = new TranslatableDataExtractorImpl(translationRulesService);
        Map<String, TranslatableContent> dataMap = dataExtractor.extractFor(node);

        assertTrue(dataMap.containsKey(path));

        TranslatableContent translatableContent = dataMap.get(path);

        assertNotNull(translatableContent);
        assertEquals(path, translatableContent.getPath());

        assertEquals(props, translatableContent.getProperties());
    }

    @Test
    void multiplePropertiesShouldBeCommaSeparatedString() throws BlackbirdInternalErrorException, RepositoryException {
        String path = "/content/test";
        Map<String, Object> props = new HashMap<>();
        props.put("foo", new String[] {"one", "two", "three"});

        context.create().resource(path, props);

        when(translationRulesService.isTranslatable(any(Node.class))).thenReturn(TranslationRulesService.IsNodeTranslatable.TRANSLATABLE);
        when(translationRulesService.isTranslatable(any(Property.class)))
                .thenReturn(false);
        when(translationRulesService.isTranslatable(argThat((Property property) -> {
            try {
                String name = property.getName();
                return name.equals("foo");
            } catch (RepositoryException e) {
                return false;
            }

        }))).thenReturn(true);

        Resource resource = context.resourceResolver().getResource(path);

        Node node = resource.adaptTo(Node.class);
        assertNotNull(node);

        TranslatableDataExtractorImpl dataExtractor = new TranslatableDataExtractorImpl(translationRulesService);
        Map<String, TranslatableContent> dataMap = dataExtractor.extractFor(node);

        System.out.println(dataMap);

        assertTrue(dataMap.containsKey(path));

        TranslatableContent translatableContent = dataMap.get(path);

        assertNotNull(translatableContent);
        assertEquals(path, translatableContent.getPath());

        assertEquals(String.join(", ", (String[])props.get("foo")), translatableContent.getProperties().get("foo"));
    }

    @Test
    void shouldReturnEmptyMapWhenTranslationRulesServiceIsNull() {
        String path = "/content/test";
        Map<String, String> props = new HashMap<>();
        props.put("foo", "foo value");
        props.put("bar", "bar value");
        context.create().resource(path, props);

        Resource resource = context.resourceResolver().getResource(path);

        Node node = resource.adaptTo(Node.class);
        assertNotNull(node);


        TranslatableDataExtractorImpl dataExtractor = new TranslatableDataExtractorImpl(null);
        Map<String, TranslatableContent> dataMap = dataExtractor.extractFor(node);

        assertTrue(dataMap.isEmpty());
    }
}