package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.dto.TranslationRules;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;
import javax.jcr.Property;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

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

    TranslationRules getTranslationsRules() throws BlackbirdInternalErrorException;

    Optional<InputStream> getTranslationRulesFileInputStream();

    List<String> collectTranslatableProperties(Node node);
}
