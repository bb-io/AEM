package io.blackbird.aemconnector.core.services.impl.detectors;

import com.adobe.cq.dam.cfm.ContentFragment;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeDetector;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

@Component(service = ContentTypeDetector.class)
public class ContentFragmentDetector implements ContentTypeDetector {
    @Override
    public boolean detects(Resource resource) {
        ContentFragment contentFragment = resource.adaptTo(ContentFragment.class);
        return ObjectUtils.isNotEmpty(contentFragment);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.CONTENT_FRAGMENT;
    }
}
