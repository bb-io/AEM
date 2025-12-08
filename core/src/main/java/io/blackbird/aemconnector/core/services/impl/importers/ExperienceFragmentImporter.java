package io.blackbird.aemconnector.core.services.impl.importers;

import com.day.cq.wcm.api.Page;
import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdPageCopyMergeService;
import io.blackbird.aemconnector.core.services.ContentImporter;
import io.blackbird.aemconnector.core.services.ContentType;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ContentImporter.class)
public class ExperienceFragmentImporter implements ContentImporter {

    @Reference
    private transient BlackbirdPageCopyMergeService pageCopyMergeService;

    @Override
    public boolean canImport(ContentType contentType) {
        return ContentType.EXPERIENCE_FRAGMENT == contentType;
    }

    @Override
    public Resource importResource(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references) throws BlackbirdServiceException {
        try {
            Page page = pageCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent, references);
            return page.adaptTo(Resource.class);
        } catch (BlackbirdResourceCopyMergeException ex) {
            throw new BlackbirdServiceException(String.format("Can not import experience fragment, sourcePath: %s, targetPath: %s", sourcePath, targetPath), ex);
        }
    }

    @Override
    public Resource importResource(String sourcePath, String targetPath, String targetContent) throws BlackbirdServiceException {
        throw new BlackbirdServiceException(String.format("Can not import experience fragment in xml format, sourcePath: %s, targetPath: %s", sourcePath, targetPath));
    }
}
