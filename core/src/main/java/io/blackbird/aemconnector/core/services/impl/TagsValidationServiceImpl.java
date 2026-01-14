package io.blackbird.aemconnector.core.services.impl;

import com.day.cq.tagging.TagManager;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.TagsValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Set;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component(service = TagsValidationService.class)
public class TagsValidationServiceImpl implements TagsValidationService {

    @Reference
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Override
    public void validateTags(Set<String> tags) throws BlackbirdServiceException {
        try (final ResourceResolver resolver = serviceUserResolverProvider.getTranslationWriterResolver()) {
            TagManager tagManager = requireNonNull(resolver.adaptTo(TagManager.class), "Can not validate tags. TagManager is null");
            for (String tagId : tags) {
                if (tagManager.resolve(tagId) == null) {
                    throw new BlackbirdServiceException(String.format("Tags validation fails. Tag does not exist: '%s'", tagId));
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BlackbirdServiceException(ex.getMessage());
        }
    }
}
