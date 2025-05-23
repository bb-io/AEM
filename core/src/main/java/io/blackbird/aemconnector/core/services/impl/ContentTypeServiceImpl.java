package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeDetector;
import io.blackbird.aemconnector.core.services.ContentTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component(service = ContentTypeService.class, immediate = true)
public class ContentTypeServiceImpl implements ContentTypeService {

    @Reference
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Reference(
            service = ContentTypeDetector.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            bind = "bindDetector",
            unbind = "unbindDetector")
    private final Set<ContentTypeDetector> detectors = new CopyOnWriteArraySet<>();

    protected void bindDetector(ContentTypeDetector detector) {
        detectors.add(detector);
    }

    protected void unbindDetector(ContentTypeDetector detector) {
        detectors.remove(detector);
    }

    @Override
    public ContentType resolveContentType(String path) {
        try (ResourceResolver resolver = serviceUserResolverProvider.getContentStructureReaderResolver()) {
            Resource resource = resolver.getResource(path);

            if (resource == null) {
                return ContentType.UNKNOWN;
            }

            return detectors.stream()
                    .filter(detector -> detector.detects(resource))
                    .map(ContentTypeDetector::getContentType)
                    .peek(contentType ->
                            log.info("Content type detected: {} for path: {}", contentType, path))
                    .findFirst().orElse(ContentType.UNKNOWN);

        } catch (LoginException e) {
            log.error("Failed to resolve resource for path: {}", path);
            throw new BlackbirdServiceException(e.getMessage(), e);
        }
    }
}
