package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.objects.TranslatableContent;
import io.blackbird.aemconnector.core.services.TranslatableDataExtractor;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.utils.TranslationUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@Slf4j
@Component(service = TranslatableDataExtractor.class, immediate = true)
public class TranslatableDataExtractorImpl implements TranslatableDataExtractor {
    private final TranslationRulesService translationRulesService;

    @Activate
    public TranslatableDataExtractorImpl(@Reference TranslationRulesService translationRulesService) {
        this.translationRulesService = translationRulesService;
    }

    @Override
    public Map<String, TranslatableContent> extractFor(@NotNull Node node) {
        final Map<String, TranslatableContent> translatableData = new HashMap<>();

        try {
            TranslationUtils.walkTranslatableProperties(node, translationRulesService, (componentNode, property) -> {
                try {

                    String path = componentNode.getPath();
                    String name = property.getName();
                    String value = getPropertyValue(property);

                    TranslatableContent translatableProperty = new TranslatableContent(path, Collections.singletonMap(name, value));

                    translatableData.merge(path, translatableProperty, (oldVal, newVal) -> {
                        Map<String, String> merged = new HashMap<>(oldVal.getProperties());
                        merged.putAll(newVal.getProperties());
                        return new TranslatableContent(path, Collections.unmodifiableMap(merged));
                    });

                } catch (RepositoryException e) {
                    log.error(e.getMessage());
                }
            });
        } catch (BlackbirdInternalErrorException e) {
            log.error(e.getMessage());
        }
        return Collections.unmodifiableMap(translatableData);
    }

    private String getPropertyValue(Property property) throws RepositoryException {
        if (property.isMultiple()) {
            Value[] values = property.getValues();
            StringJoiner vals = new StringJoiner(", ");

            for (Value value : values) {
                String valStr = value.getString();
                vals.add(valStr);
            }
            return vals.toString();
        } else {
            Value value = property.getValue();
            return value.getString();
        }
    }
}
