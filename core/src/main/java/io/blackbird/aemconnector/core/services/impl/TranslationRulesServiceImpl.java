package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.dto.TranslationRules;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.services.impl.rules.AssetReferenceRule;
import io.blackbird.aemconnector.core.services.impl.rules.ContextRule;
import io.blackbird.aemconnector.core.services.impl.rules.TranslationRulesFileParser;
import io.blackbird.aemconnector.core.utils.MemoizingSupplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.Property;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.apache.jackrabbit.JcrConstants.JCR_CONTENT;
import static org.apache.jackrabbit.JcrConstants.JCR_DATA;

@Slf4j
@Component(service = TranslationRulesService.class, immediate = true)
public class TranslationRulesServiceImpl implements TranslationRulesService {

    private static final String CONF_RULES_FILE_PATH = "/conf/global/settings/translation/rules/translation_rules.xml";
    private static final String APPS_RULES_FILE_PATH = "/apps/settings/translation/rules/translation_rules.xml";
    private static final String LEGACY_ETC_RULES_FILE_PATH = "/etc/workflow/models/translation/translation_rules.xml";
    private static final String LIBS_RULES_FILE_PATH = "/libs/settings/translation/rules/translation_rules.xml";
    private static final List<String> RULE_FILES_PRIORITY_LIST = Arrays.asList(
            CONF_RULES_FILE_PATH,
            APPS_RULES_FILE_PATH,
            LEGACY_ETC_RULES_FILE_PATH,
            LIBS_RULES_FILE_PATH);

    @Reference
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    private Supplier<TranslationRules> translationRulesSupplier;

    @Activate
    protected void activate() {
        translationRulesSupplier =
                MemoizingSupplier.memoizeWithExpiration(this::readTranslationRules, 1, TimeUnit.MINUTES);
    }

    @Override
    public boolean isTranslatable(Property property) throws BlackbirdInternalErrorException {
        for (ContextRule rule : getTranslationsRules().getContextRules()) {
            if (rule.appliesTo(property)) {
                return rule.isTranslatable(property);
            }
        }
        return false;
    }

    @Override
    public IsNodeTranslatable isTranslatable(Node node) throws BlackbirdInternalErrorException {
        for (ContextRule rule : getTranslationsRules().getContextRules()) {
            if (rule.appliesTo(node)) {
                return rule.isTranslatable(node);
            }
        }
        return IsNodeTranslatable.TRANSLATABLE;
    }

    @Override
    public IsAssetReference isAssetReference(Property property) throws BlackbirdInternalErrorException {
        for (AssetReferenceRule rule : getTranslationsRules().getAssetReferenceRules()) {
            IsAssetReference assetReference = rule.isAssetReference(property);
            if (null != assetReference) {
                return assetReference;
            }
        }
        return IsAssetReference.NOT_REFERENCE;
    }

    private TranslationRules getTranslationsRules() throws BlackbirdInternalErrorException {
        TranslationRules translationRules = translationRulesSupplier.get();

        if (TranslationRules.EMPTY.equals(translationRules)) {
            throw new BlackbirdInternalErrorException("No translation rules found");
        }

        return translationRules;
    }

    private TranslationRules readTranslationRules() {
        log.debug("Reading translation rules file");

        try (ResourceResolver resolver = serviceUserResolverProvider.getTranslationRulesReaderResolver()) {
            Optional<InputStream> translationRulesInputStream = getTranslationRulesFileInputStream(resolver);
            if (!translationRulesInputStream.isPresent()) {
                log.trace("No translation rules InputStream found");
                return TranslationRules.EMPTY;
            }
            try (InputStream is = translationRulesInputStream.get()) {
                return new TranslationRulesFileParser().parse(is);
            }
        } catch (LoginException | IOException e) {
            log.error("Failed to read translation rules file", e);
            return TranslationRules.EMPTY;
        }
    }

    /**
     * Finds the first available translation rules file binary based on priority order.
     *
     * @return Optional containing the binary if found, empty otherwise
     */
    private Optional<InputStream> getTranslationRulesFileInputStream(ResourceResolver resolver) {
        log.debug("Searching for translation rules file in priority order");

        return RULE_FILES_PRIORITY_LIST.stream()
                .map(filePath -> {
                    Resource resource = resolver.getResource(filePath);
                    if (resource != null) {
                        log.debug("Found translation rules file at: {}", filePath);
                    }
                    return resource;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .map(r -> r.getChild(JCR_CONTENT))
                .map(Resource::getValueMap)
                .map(m -> m.get(JCR_DATA, InputStream.class));
    }

}
