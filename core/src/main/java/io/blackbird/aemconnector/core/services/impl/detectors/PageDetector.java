package io.blackbird.aemconnector.core.services.impl.detectors;

import com.day.cq.wcm.api.constants.NameConstants;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeDetector;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.util.Optional;

@Component(service = ContentTypeDetector.class)
public class PageDetector implements ContentTypeDetector {
    @Override
    public boolean detects(Resource resource) {

        return Optional.ofNullable(resource)
                .filter(res -> res.isResourceType(NameConstants.NT_PAGE))
                .map(Resource::getPath)
                .filter(path -> path.startsWith("/content") && !path.contains("experience-fragments"))
                .isPresent();
    }

    @Override
    public ContentType getContentType() {
        return ContentType.PAGE;
    }
}
