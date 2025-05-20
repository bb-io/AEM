package io.blackbird.aemconnector.core.services.impl.rules;

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

    private final String name;
    private final boolean translate;
    private final boolean inherit;
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
