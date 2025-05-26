package io.blackbird.aemconnector.core.services.impl.detectors;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.DamConstants;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeDetector;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

@Component(service = ContentTypeDetector.class)
public class ContentFragmentDetector implements ContentTypeDetector {
    @Override
    public boolean detects(Resource resource) {

        if (resource == null || !resource.isResourceType(DamConstants.NT_DAM_ASSET)) {
            return false;
        }

        Resource jcrContent = resource.getChild(JcrConstants.JCR_CONTENT);
        if (jcrContent == null) {
            return false;
        }

        ValueMap valueMap = jcrContent.getValueMap();
        Boolean isContentFragment = valueMap.get("contentFragment", Boolean.class);
        return Boolean.TRUE.equals(isContentFragment);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.CONTENT_FRAGMENT;
    }
}
