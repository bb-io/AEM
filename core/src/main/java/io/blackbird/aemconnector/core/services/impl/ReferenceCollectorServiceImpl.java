package io.blackbird.aemconnector.core.services.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import io.blackbird.aemconnector.core.dto.ContentReference;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.ReferenceCollectorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component(service = ReferenceCollectorService.class)
public class ReferenceCollectorServiceImpl implements ReferenceCollectorService {

    private static final Set<String> REFERENCE_PROPERTY_NAMES = new HashSet<>(Arrays.asList(
            "fileReference",
            "pageReference",
            "fragmentPath",
            "fragmentVariationPath"
    ));
    private static final String STRUCTURE_JCR_CONTENT = "/structure/jcr:content";

    @Reference
    private BlackbirdServiceUserResolverProvider resolverProvider;

    @Override
    public List<ContentReference> getReferences(String rootPath) {
        try (ResourceResolver resolver = resolverProvider.getReferenceReaderResolver()) {
            Resource rootResource = resolver.getResource(rootPath);
            if (rootResource == null) {
                return Collections.emptyList();
            }
            List<ContentReference> references = new ArrayList<>();
            collectAllReferences(rootResource, resolver, references);
            return references;
        } catch (LoginException ex) {
            log.error("Cannot access Reference Reader", ex);
        }
        return Collections.emptyList();
    }

    private void collectAllReferences(Resource resource, ResourceResolver resolver, List<ContentReference> references) {
        collectReferencesFromPageTemplate(resource, resolver, references);
        collectReferencesFromProperties(resource, resolver, references);
    }

    private void collectReferencesFromPageTemplate(Resource resource, ResourceResolver resolver, List<ContentReference> references) {
        Page page = resource.adaptTo(Page.class);
        if (page == null) {
            return;
        }
        Template template = page.getTemplate();
        if (template == null) {
            return;
        }
        Resource templateStructureResource = resolver.getResource(template.getPath() + STRUCTURE_JCR_CONTENT);
        if (templateStructureResource == null) {
            return;
        }
        collectReferencesFromProperties(templateStructureResource, resolver, references);
    }

    private void collectReferencesFromProperties(Resource resource, ResourceResolver resolver, List<ContentReference> references) {
        ValueMap properties = resource.getValueMap();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (REFERENCE_PROPERTY_NAMES.contains(entry.getKey())) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    addReference((String) value, resolver, references);
                } else if (value instanceof String[]) {
                    for (String val : (String[]) value) {
                        addReference(val, resolver, references);
                    }
                }
            }
        }
        for (Resource child : resource.getChildren()) {
            collectReferencesFromProperties(child, resolver, references);
        }
    }

    private void addReference(String value, ResourceResolver resolver, List<ContentReference> references) {
        Resource referenceResource = resolver.getResource(value);
        if (referenceResource == null) {
            return;
        }
        ContentReference contentReference = new ContentReference(value);
        List<ContentReference> childReferences = new ArrayList<>();
        collectReferencesFromProperties(referenceResource, resolver, childReferences);
        contentReference.getReferences().addAll(childReferences);
        references.add(contentReference);
    }
}
