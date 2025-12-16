package io.blackbird.aemconnector.core.utils;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.function.BiConsumer;

import static io.blackbird.aemconnector.core.services.TranslationRulesService.IsNodeTranslatable.TRANSLATABLE;

@Slf4j
public final class TranslationUtils {
    private TranslationUtils() {
    }

    public static void walkTranslatableProperties(@NotNull Node node,
                                             TranslationRulesService translationRulesService,
                                             BiConsumer<Node, Property> callback) throws BlackbirdInternalErrorException {
        if (null == translationRulesService) {
            log.error("TranslationRulesService is null");
            throw new BlackbirdInternalErrorException("TranslationRulesService is null");
        }

        try {
            TranslationRulesService.IsNodeTranslatable isNodeTranslatable = translationRulesService.isTranslatable(node);

            if (TRANSLATABLE.equals(isNodeTranslatable)) {
                PropertyIterator propertyIterator = node.getProperties();
                while (propertyIterator.hasNext()) {
                    Property property = propertyIterator.nextProperty();
                    String key = property.getName();

                    if (!translationRulesService.isTranslatable(property)) {
                        log.trace("Property {}/{} is not translatable, skipping", node.getPath(), key);
                        continue;
                    }
                    callback.accept(node, property);
                }
            }
        } catch (RepositoryException e) {
            throw new BlackbirdInternalErrorException("Error accessing JCR node: " + e.getMessage());
        }
    }
}
