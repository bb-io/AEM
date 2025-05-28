package io.blackbird.aemconnector.core.services.impl.detectors;

import com.adobe.cq.xf.ExperienceFragmentVariation;
import com.day.cq.wcm.api.Page;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeDetector;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.util.Optional;

@Component(service = ContentTypeDetector.class)
public class ExperienceFragmentDetector implements ContentTypeDetector {
    @Override
    public boolean detects(Resource resource) {
        return Optional.ofNullable(resource)
                .map(res -> res.adaptTo(Page.class))
                .map(page -> page.adaptTo(ExperienceFragmentVariation.class))
                .isPresent();
    }

    @Override
    public ContentType getContentType() {
        return ContentType.EXPERIENCE_FRAGMENT;
    }
}
