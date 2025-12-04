package io.blackbird.aemconnector.core.services.impl.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.utils.RepositoryUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.jcr.Node;
import javax.jcr.Property;
import java.io.Serializable;
import java.util.List;

import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;

@Builder
@EqualsAndHashCode(callSuper = false)
@ToString
public class ResourceTypeRule implements TranslationPropertyRule {

    @JsonProperty
    private final String resourceType;
    @JsonProperty
    private final List<TranslationPropertyRule> propertyNameRules;

    @Override
    public boolean appliesTo(Property property) throws BlackbirdInternalErrorException {
        Node resourceTypeNode = RepositoryUtils.getParentWithProperty(property, SLING_RESOURCE_TYPE_PROPERTY);

        if (null != resourceTypeNode
                && resourceType.equals(RepositoryUtils.getPropertyAsString(resourceTypeNode, SLING_RESOURCE_TYPE_PROPERTY))) {
            for (TranslationPropertyRule rule : propertyNameRules) {
                if (rule.appliesTo(property)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isTranslatable(Property property) throws BlackbirdInternalErrorException {
        for (TranslationPropertyRule rule : propertyNameRules) {
            if (rule.appliesTo(property)) {
                return rule.isTranslatable(property);
            }
        }
        return false;
    }

}
