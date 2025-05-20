package io.blackbird.aemconnector.core.services.impl.rules;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.utils.RepositoryUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.jcr.Node;
import javax.jcr.Property;
import java.util.List;

@Builder
@EqualsAndHashCode(callSuper = false)
@ToString
public class ContextRule {

    @Getter
    private final String contextPath;

    private final List<TranslationNodeFilterRule> translationNodeFilterRules;
    private final List<TranslationPropertyRule> propertyFilterRules;
    private final List<TranslationPropertyRule> resourceTypeRules;
    private final List<TranslationPropertyRule> generalRules;

    public boolean appliesTo(Property property) throws BlackbirdInternalErrorException {
        return RepositoryUtils.getPath(property).startsWith(contextPath);
    }

    public boolean isTranslatable(Property property) throws BlackbirdInternalErrorException {
        for (TranslationPropertyRule rule : propertyFilterRules) {
            if (rule.appliesTo(property)) {
                return rule.isTranslatable(property);
            }
        }
        for (TranslationPropertyRule rule : resourceTypeRules) {
            if (rule.appliesTo(property)) {
                return rule.isTranslatable(property);
            }
        }
        for (TranslationPropertyRule rule : generalRules) {
            if (rule.appliesTo(property)) {
                return rule.isTranslatable(property);
            }
        }
        return false;
    }

    public boolean appliesTo(Node node) throws BlackbirdInternalErrorException {
        return RepositoryUtils.getPath(node).startsWith(contextPath);
    }

    public TranslationRulesService.IsNodeTranslatable isTranslatable(Node node) throws BlackbirdInternalErrorException {
        for (TranslationNodeFilterRule rule : translationNodeFilterRules) {
            if (rule.appliesTo(node)) {
                return rule.isTranslatable();
            }
        }
        return TranslationRulesService.IsNodeTranslatable.TRANSLATABLE;
    }

}
