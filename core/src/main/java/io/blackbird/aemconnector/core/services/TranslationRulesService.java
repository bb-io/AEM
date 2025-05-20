package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;

import javax.jcr.Node;
import javax.jcr.Property;

public interface TranslationRulesService {

    enum IsNodeTranslatable {
        TRANSLATABLE,
        ONLY_CHILDREN_TRANSLATABLE,
        NON_TRANSLATABLE
    }

    enum IsAssetReference {
        REFERENCE,
        REFERENCE_WITH_CHILDREN,
        NOT_REFERENCE
    }

    boolean isTranslatable(Property property) throws BlackbirdInternalErrorException;

    IsNodeTranslatable isTranslatable(Node node) throws BlackbirdInternalErrorException;

    IsAssetReference isAssetReference(Property property) throws BlackbirdInternalErrorException;

}
