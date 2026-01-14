package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.UpdateTagsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component(service = UpdateTagsService.class)
public class UpdateTagsServiceImpl implements UpdateTagsService {

    private final static String CQ_TAGS = "cq:tags";
    private static final String JCR_CONTENT = "/jcr:content";
    private static final String JCR_CONTENT_METADATA = "/jcr:content/metadata";

    @Reference
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Override
    public Resource updateTags(String contentPath, Set<String> tagsToAdd, Set<String> tagsToRemove, ContentType contentType) throws BlackbirdServiceException {
        try (final ResourceResolver resolver = serviceUserResolverProvider.getTranslationWriterResolver()) {
            Resource resource = requireNonNull(resolver.getResource(contentPath),
                    String.format("Resource does not exist '%s'", contentPath));
            Resource childResource = requireNonNull(getResourceByContentType(resolver, contentPath, contentType),
                    String.format("Child resource does not exist '%s'", contentPath));
            ModifiableValueMap properties = requireNonNull(childResource.adaptTo(ModifiableValueMap.class),
                    String.format("Can not adapt resource to modifiableValueMap '%s'", childResource.getPath()));
            String[] existingTags = properties.get(CQ_TAGS, String[].class);
            Set<String> updatedTags = new HashSet<>();
            if (existingTags != null) {
                updatedTags.addAll(Arrays.asList(existingTags));
            }
            if (tagsToRemove != null) {
                updatedTags.removeAll(tagsToRemove);
            }
            if (tagsToAdd != null) {
                updatedTags.addAll(tagsToAdd);
            }
            properties.put(CQ_TAGS, updatedTags.toArray(new String[0]));
            resolver.commit();
            return resource;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BlackbirdServiceException(ex.getMessage());
        }
    }

    private Resource getResourceByContentType(ResourceResolver resolver, String path, ContentType contentType) {
        if (ContentType.PAGE == contentType || ContentType.EXPERIENCE_FRAGMENT == contentType) {
            return resolver.getResource(path.concat(JCR_CONTENT));
        } else if (ContentType.ASSET == contentType || ContentType.CONTENT_FRAGMENT == contentType || ContentType.DITA == contentType) {
            return resolver.getResource(path.concat(JCR_CONTENT_METADATA));
        } else {
            return null;
        }
    }

}
