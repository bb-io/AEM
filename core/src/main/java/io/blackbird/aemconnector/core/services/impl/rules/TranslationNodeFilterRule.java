package io.blackbird.aemconnector.core.services.impl.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.utils.RepositoryUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.jcr.Node;

import java.io.Serializable;

import static io.blackbird.aemconnector.core.utils.RepositoryUtils.ROOT_PATH;
import static io.blackbird.aemconnector.core.utils.RepositoryUtils.getParent;

@Builder
@EqualsAndHashCode(callSuper = false)
@ToString
public class TranslationNodeFilterRule {

    @JsonProperty
    private final String propertyName;
    @JsonProperty
    private final String propertyValue;
    @JsonProperty
    private final boolean isDeep;

    public boolean appliesTo(Node node) throws BlackbirdInternalErrorException {
        Node parent = node;
        do {
            if (RepositoryUtils.hasProperty(parent, propertyName)
                    && propertyValue.equals(RepositoryUtils.getPropertyAsString(parent, propertyName))) {
                return true;
            }
            parent = getParent(parent);
        } while (isDeep && null != parent && !ROOT_PATH.equals(RepositoryUtils.getPath(parent)));
        return false;
    }

    public TranslationRulesService.IsNodeTranslatable isTranslatable() {
        return isDeep ? TranslationRulesService.IsNodeTranslatable.NON_TRANSLATABLE : TranslationRulesService.IsNodeTranslatable.ONLY_CHILDREN_TRANSLATABLE;
    }

}
