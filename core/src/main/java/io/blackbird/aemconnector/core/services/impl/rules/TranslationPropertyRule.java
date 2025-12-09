package io.blackbird.aemconnector.core.services.impl.rules;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;

import javax.jcr.Property;
import java.io.Serializable;

public interface TranslationPropertyRule extends Serializable {

    boolean appliesTo(Property property) throws BlackbirdInternalErrorException;

    boolean isTranslatable(Property property) throws BlackbirdInternalErrorException;

}
