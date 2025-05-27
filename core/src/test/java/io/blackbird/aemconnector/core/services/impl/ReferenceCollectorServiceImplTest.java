package io.blackbird.aemconnector.core.services.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import io.blackbird.aemconnector.core.dto.ContentReference;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Value;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReferenceCollectorServiceImplTest {

    private ReferenceCollectorServiceImpl service;
    private BlackbirdServiceUserResolverProvider resolverProvider;
    private TranslationRulesService translationRulesService;
    private ResourceResolver resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolverProvider = mock(BlackbirdServiceUserResolverProvider.class);
        translationRulesService = mock(TranslationRulesService.class);
        resolver = mock(ResourceResolver.class);

        service = new ReferenceCollectorServiceImpl();
        PrivateAccessor.setField(service, "resolverProvider", resolverProvider);
        PrivateAccessor.setField(service, "translationRulesService", translationRulesService);

        when(resolverProvider.getReferenceReaderResolver()).thenReturn(resolver);
    }

    @Test
    void shouldReturnEmptyReferencesWhenPageNotExists() {
        when(resolver.getResource("/content/bb-aem-connector")).thenReturn(null);

        List<ContentReference> results = service.getReferences("/content/bb-aem-connector");

        assertEquals(0, results.size());
    }

    @Test
    void shouldReturnReferencesWhenTemplateContainsReferences() throws Exception {
        Resource rootResource = mock(Resource.class);
        Node rootNode = mock(Node.class);
        PropertyIterator propertyIterator = mock(PropertyIterator.class);
        Property property = mock(Property.class);
        Value value = mock(Value.class);
        Resource damResource = mock(Resource.class);
        Node damNode = mock(Node.class);

        when(resolver.getResource("/content/bb-aem-connector")).thenReturn(rootResource);
        when(rootResource.adaptTo(Node.class)).thenReturn(rootNode);
        when(rootNode.getProperties()).thenReturn(propertyIterator);
        when(propertyIterator.hasNext()).thenReturn(true, false);
        when(propertyIterator.nextProperty()).thenReturn(property);
        when(translationRulesService.isAssetReference(property)).thenReturn(TranslationRulesService.IsAssetReference.REFERENCE);
        when(property.isMultiple()).thenReturn(false);
        when(property.getValue()).thenReturn(value);
        when(value.getString()).thenReturn("/content/reference/image");
        when(resolver.getResource("/content/reference/image")).thenReturn(damResource);
        when(damResource.adaptTo(Node.class)).thenReturn(damNode);
        when(damNode.getProperties()).thenReturn(mock(PropertyIterator.class));

        List<ContentReference> results = service.getReferences("/content/bb-aem-connector");

        assertEquals(1, results.size());
        assertEquals("/content/reference/image", results.get(0).getPath());
    }

    @Test
    void shouldReturnReferencesWhenPageContainsReferences() throws Exception {
        Resource rootResource = mock(Resource.class);
        Page rootPage = mock(Page.class);
        Template template = mock(Template.class);
        Resource templateResource = mock(Resource.class);
        Node templateNode = mock(Node.class);
        PropertyIterator propertyIterator = mock(PropertyIterator.class);
        Property property = mock(Property.class);
        Value value = mock(Value.class);
        Resource xfResource = mock(Resource.class);
        Node xfNode = mock(Node.class);

        when(resolver.getResource("/content/bb-aem-connector")).thenReturn(rootResource);
        when(rootResource.adaptTo(Page.class)).thenReturn(rootPage);
        when(rootPage.getTemplate()).thenReturn(template);
        when(template.getPath()).thenReturn("/conf/bb-aem-connector/templates/page-content");
        when(resolver.getResource("/conf/bb-aem-connector/templates/page-content/structure/jcr:content")).thenReturn(templateResource);
        when(templateResource.adaptTo(Node.class)).thenReturn(templateNode);
        when(templateNode.getProperties()).thenReturn(propertyIterator);
        when(propertyIterator.hasNext()).thenReturn(true, false);
        when(propertyIterator.nextProperty()).thenReturn(property);
        when(translationRulesService.isAssetReference(property)).thenReturn(TranslationRulesService.IsAssetReference.REFERENCE);
        when(property.isMultiple()).thenReturn(false);
        when(property.getValue()).thenReturn(value);
        when(value.getString()).thenReturn("/content/experience-fragments/header/master");
        when(resolver.getResource("/content/experience-fragments/header/master")).thenReturn(xfResource);
        when(xfResource.adaptTo(Node.class)).thenReturn(xfNode);
        when(xfNode.getProperties()).thenReturn(mock(PropertyIterator.class));

        List<ContentReference> results = service.getReferences("/content/bb-aem-connector");

        assertEquals(1, results.size());
        assertEquals("/content/experience-fragments/header/master", results.get(0).getPath());
    }

}
