package io.blackbird.aemconnector.core.services.impl.rules;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.utils.RepositoryUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.Property;

import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;

@Builder
@EqualsAndHashCode(callSuper = false)
@ToString
public class AssetReferenceRule {

    private final String assetReferenceAttribute;
    private final String resourceType;
    private final boolean checkInChildNodes;
    private final boolean createLangCopy;

    public TranslationRulesService.IsAssetReference isAssetReference(Property property) throws BlackbirdInternalErrorException {
        Node parent = RepositoryUtils.getParent(property);
        if (null == parent) {
            throw new BlackbirdInternalErrorException("Can't get parent node of property " + RepositoryUtils.getPath(property));
        }

        if (assetReferenceAttribute.equals(RepositoryUtils.getName(property))
                && isResourceTypeEqualsParentResourceType(resourceType, parent)) {
            return checkInChildNodes ? TranslationRulesService.IsAssetReference.REFERENCE_WITH_CHILDREN : TranslationRulesService.IsAssetReference.REFERENCE;
        }
        return null;
    }

    private boolean isResourceTypeEqualsParentResourceType(String resourceType, Node parent) throws BlackbirdInternalErrorException {
        if (resourceType == null || parent == null) {
            return false;
        }

        String parentResourceType = RepositoryUtils.hasProperty(parent, SLING_RESOURCE_TYPE_PROPERTY)
                ? RepositoryUtils.getPropertyAsString(parent, SLING_RESOURCE_TYPE_PROPERTY)
                : StringUtils.EMPTY;
        String primaryType = RepositoryUtils.getPrimaryNodeTypeAsString(parent);

        return resourceType.equals(parentResourceType) || primaryType.equals(resourceType);
    }
}
