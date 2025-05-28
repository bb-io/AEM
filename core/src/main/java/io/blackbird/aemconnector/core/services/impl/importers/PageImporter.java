package io.blackbird.aemconnector.core.services.impl.importers;

import com.day.cq.wcm.api.Page;
import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdPageCopyMergeException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdPageCopyMergeService;
import io.blackbird.aemconnector.core.services.ContentImporter;
import io.blackbird.aemconnector.core.services.ContentType;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ContentImporter.class)
public class PageImporter implements ContentImporter {

    @Reference
    private transient BlackbirdPageCopyMergeService pageCopyMergeService;

    @Override
    public boolean canImport(ContentType contentType) {
        return ContentType.PAGE == contentType;
    }

    @Override
    public Resource importResource(String sourcePath, String targetPath, JsonNode targetContent) throws BlackbirdServiceException {
        try {
            Page page = pageCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent);
            return page.adaptTo(Resource.class);
        } catch (BlackbirdPageCopyMergeException | LoginException ex) {
            throw new BlackbirdServiceException(String.format("Can not import page, sourcePath: %s, targetPath: %s", sourcePath, targetPath), ex);
        }
    }
}
