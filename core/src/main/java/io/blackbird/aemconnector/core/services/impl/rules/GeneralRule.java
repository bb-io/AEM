package io.blackbird.aemconnector.core.services.impl.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.utils.RepositoryUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.jcr.Property;

@Builder
@EqualsAndHashCode(callSuper = false)
@ToString
public class GeneralRule implements TranslationPropertyRule {

    @JsonProperty("propertyName")
    private final String name;
    @JsonProperty
    private final boolean translate;
    @JsonProperty
    private final boolean inherit;
    @JsonProperty
    private final boolean updateDestinationLanguage;

    @Override
    public boolean appliesTo(Property property) throws BlackbirdInternalErrorException {
        return name.equals(RepositoryUtils.getName(property));
    }

    @Override
    public boolean isTranslatable(Property property) {
        return translate;
    }

}
