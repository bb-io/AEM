package io.blackbird.aemconnector.core.services.impl.detectors;

import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeDetector;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

@Component(service = ContentTypeDetector.class)
public class PageDetector implements ContentTypeDetector {
    @Override
    public boolean detects(Resource resource) {
        return false;
    }

    @Override
    public ContentType getContentType() {
        return ContentType.PAGE;
    }
}
