package io.blackbird.aemconnector.core.services.impl.detectors;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.DamConstants;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeDetector;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

@Component(service = ContentTypeDetector.class)
public class AssetDetector implements ContentTypeDetector {
    @Override
    public boolean detects(Resource resource) {
        if (resource == null) {
            return false;
        }

        Resource jcrContent = resource.getChild(JcrConstants.JCR_CONTENT);
        if (jcrContent == null) {
            return false;
        }

        boolean isDamAsset = resource.isResourceType(DamConstants.NT_DAM_ASSET);

        ValueMap valueMap = jcrContent.getValueMap();
        boolean isContentFragment = valueMap.get("contentFragment", false);

        return isDamAsset && !isContentFragment;
    }

    @Override
    public ContentType getContentType() {
        return ContentType.ASSET;
    }
}
