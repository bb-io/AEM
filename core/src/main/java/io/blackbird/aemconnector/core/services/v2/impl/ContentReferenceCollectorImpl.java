package io.blackbird.aemconnector.core.services.v2.impl;

import io.blackbird.aemconnector.core.dto.v2.ContentReference;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.exceptions.ContentReferenceException;
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

        if (resource == null) {
            log.warn("Reference collection requested with null resource");
            return Collections.emptyList();
        }

        Node root = resource.adaptTo(Node.class);

        if (root == null) {
            log.warn("Could not adapt resource [{}] to JCR Node", resource.getPath());
            return Collections.emptyList();
        }

        List<ContentReference> references = new ArrayList<>();

        try {
            collectReferencesFromProperties(root, references);
        } catch (RepositoryException | BlackbirdInternalErrorException e) {
            String nodePath = getSafePath(root);
            log.error("Failed collecting content references from node [{}]: {}", nodePath, e.getMessage(), e);
            throw new ContentReferenceException("Error collecting content references from node: " + nodePath, e);
        }

        return references;
    }

    private void collectReferencesFromProperties(Node node, List<ContentReference> references) throws RepositoryException, BlackbirdInternalErrorException {
        PropertyIterator propertyIterator = node.getProperties();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();

            TranslationRulesService.IsAssetReference assetReference = translationRulesService.isAssetReference(property);

            if (assetReference == TranslationRulesService.IsAssetReference.NOT_REFERENCE || assetReference == null) {
                continue;
            }

            String propertyName = property.getName();
            String parentPath = node.getPath();

            if (property.isMultiple()) {
                for (Value value : property.getValues()) {
                    references.add(
                      new ContentReference(propertyName, parentPath, value.toString()));
                }
            } else {
                String referencePath = property.getValue().getString();
                references.add(
                    new ContentReference(propertyName, parentPath, referencePath));
            }
        }

        NodeIterator nodeIterator = node.getNodes();
        while (nodeIterator.hasNext()) {
            Node child = nodeIterator.nextNode();
            collectReferencesFromProperties(child, references);
        }
    }

    private String getSafePath(Node node) {
        try {
            return node.getPath();
        } catch (RepositoryException e) {
            return "[unresolvable path]";
        }
    }
}
