package io.blackbird.aemconnector.core.services.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import io.blackbird.aemconnector.core.dto.v1.ContentReference;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.v1.ReferenceCollectorService;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component(service = ReferenceCollectorService.class)
public class ReferenceCollectorServiceImpl implements ReferenceCollectorService {

    private static final String STRUCTURE_JCR_CONTENT = "/structure/jcr:content";

    @Reference
    private BlackbirdServiceUserResolverProvider resolverProvider;

    @Reference
    private TranslationRulesService translationRulesService;

    @Override
    public List<ContentReference> getReferences(String rootPath) {
        try (ResourceResolver resolver = resolverProvider.getReferenceReaderResolver()) {
            Resource rootResource = resolver.getResource(rootPath);
            if (rootResource == null) {
                return Collections.emptyList();
            }
            List<ContentReference> references = new ArrayList<>();
            collectAllReferences(rootResource, references);
            return references;
        } catch (LoginException ex) {
            log.error("Cannot access Reference Reader", ex);
        }
        return Collections.emptyList();
    }

    private void collectAllReferences(Resource resource, List<ContentReference> references) {
        collectReferencesFromPageTemplate(resource, references);
        collectReferencesFromProperties(resource, references);
    }

    private void collectReferencesFromPageTemplate(Resource resource, List<ContentReference> references) {
        Optional.ofNullable(resource.adaptTo(Page.class))
                .map(Page::getTemplate)
                .map(Template::getPath)
                .map(path -> path + STRUCTURE_JCR_CONTENT)
                .map(resource.getResourceResolver()::getResource)
                .ifPresent(res -> collectReferencesFromProperties(res, references));
    }

    private void collectReferencesFromProperties(Resource resource, List<ContentReference> references) {
        Node node = resource.adaptTo(Node.class);
        if (node == null) {
            return;
        }
        try {
            PropertyIterator propertyIterator = node.getProperties();
            while (propertyIterator.hasNext()) {
                Property property = propertyIterator.nextProperty();
                ResourceResolver resolver = resource.getResourceResolver();
                if (!TranslationRulesService.IsAssetReference.NOT_REFERENCE.equals(translationRulesService.isAssetReference(property))) {
                     if (property.isMultiple()) {
                        Value[] values = property.getValues();
                        for (Value value : values) {
                            addReference(value.getString(), resolver, references);
                        }
                    } else {
                        addReference(property.getValue().getString(), resolver, references);
                    }
                }
            }
        } catch (RepositoryException | BlackbirdInternalErrorException ex) {
            log.error(String.format("Cannot collect references from properties for resource %s", resource.getPath()), ex);
        }
        for (Resource child : resource.getChildren()) {
            collectReferencesFromProperties(child, references);
        }
    }

    private void addReference(String path, ResourceResolver resolver, List<ContentReference> references) {
        Resource referenceResource = resolver.getResource(path);
        if (referenceResource == null) {
            return;
        }
        ContentReference contentReference = new ContentReference(path);
        List<ContentReference> childReferences = new ArrayList<>();
        collectReferencesFromProperties(referenceResource, childReferences);
        contentReference.getReferences().addAll(childReferences);
        references.add(contentReference);
    }
}
