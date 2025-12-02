package io.blackbird.aemconnector.core.services.impl.importers;

import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdAssetCopyMergeService;
import io.blackbird.aemconnector.core.services.ContentImporter;
import io.blackbird.aemconnector.core.services.ContentType;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ContentImporter.class)
public class ContentFragmentImporter implements ContentImporter {

    @Reference
    private transient BlackbirdAssetCopyMergeService assetCopyMergeService;

    @Override
    public boolean canImport(ContentType contentType) {
        return ContentType.CONTENT_FRAGMENT == contentType;
    }

    @Override
    public Resource importResource(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references) throws BlackbirdServiceException {
        try {
            return assetCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent, references);
        } catch (BlackbirdResourceCopyMergeException ex) {
            throw new BlackbirdServiceException(String.format("Can not import content fragment, sourcePath: %s, targetPath: %s", sourcePath, targetPath), ex);
        }
    }

    @Override
    public Resource importResource(String sourcePath, String targetPath, String targetContent) throws BlackbirdServiceException {
        throw new BlackbirdServiceException(String.format("Can not import content fragment in xml format, sourcePath: %s, targetPath: %s", sourcePath, targetPath));
    }
}
