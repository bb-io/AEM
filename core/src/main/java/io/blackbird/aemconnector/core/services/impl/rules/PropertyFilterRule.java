package io.blackbird.aemconnector.core.services.impl.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.utils.RepositoryUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.jcr.Property;
import java.io.Serializable;

@Builder
@EqualsAndHashCode(callSuper = false)
@ToString
public class PropertyFilterRule implements TranslationPropertyRule {

    @JsonProperty
    private final String pathContains;
    @JsonProperty
    private final String propertyName;

    @Override
    public boolean appliesTo(Property property) throws BlackbirdInternalErrorException {
        return RepositoryUtils.getPath(property).contains(pathContains) && propertyName.equals(RepositoryUtils.getName(property));
    }

    @Override
    public boolean isTranslatable(Property property) {
        return false;
    }

}
