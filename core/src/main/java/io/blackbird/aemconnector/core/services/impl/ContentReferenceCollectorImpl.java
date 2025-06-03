package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.dto.v2.ContentReference;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.services.v2.ReferenceCollectorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component(service = ReferenceCollectorService.class)
public class ContentReferenceCollectorImpl implements ReferenceCollectorService {

    @Reference
    private TranslationRulesService translationRulesService;

    @Override
    public List<ContentReference> getReferences(Resource resource) {

        List<ContentReference> references = new ArrayList<>();
        Node root = resource.adaptTo(Node.class);

        if (root == null) {
            return Collections.emptyList();
        }

        try {
            collectReferencesFromProperties(root, references);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }

        return references;
    }

    private void collectReferencesFromProperties(Node node, List<ContentReference> references) throws RepositoryException {
        if (node == null) {
            return;
        }
        try {
            PropertyIterator propertyIterator = node.getProperties();
            while (propertyIterator.hasNext()) {
                Property property = propertyIterator.nextProperty();
                if (!TranslationRulesService.IsAssetReference.NOT_REFERENCE.equals(translationRulesService.isAssetReference(property))) {
                    String propertyName = property.getName();
                    String propertyPath = property.getPath();
                    if (property.isMultiple()) {
                        Value[] values = property.getValues();
                        for (Value value : values) {
                            references.add(
                              new ContentReference(propertyName, propertyPath, value.toString()));
                        }
                    } else {
                        String referencePath = property.getValue().getString();
                        references.add(
                            new ContentReference(propertyName, propertyPath, referencePath));
                    }
                }
            }
        } catch (RepositoryException | BlackbirdInternalErrorException ex) {
            log.error(String.format("Cannot collect references from properties for node %s", node.getPath()), ex);
        }

        NodeIterator nodeIterator = node.getNodes();
        while (nodeIterator.hasNext()) {
            Node child = nodeIterator.nextNode();
            collectReferencesFromProperties(child, references);
        }
    }
}
