package io.blackbird.aemconnector.core.services.impl.rules;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;

import javax.jcr.Property;

public interface TranslationPropertyRule {

    boolean appliesTo(Property property) throws BlackbirdInternalErrorException;

    boolean isTranslatable(Property property) throws BlackbirdInternalErrorException;

}
