package io.blackbird.aemconnector.core.services.impl.importers;

import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.exceptions.CopyMergeDitaValidationException;
import io.blackbird.aemconnector.core.services.ContentImporter;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.DitaCopyMergeService;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ContentImporter.class)
public class DitaImporter implements ContentImporter {

    @Reference
    private transient DitaCopyMergeService ditaCopyMergeService;

    @Override
    public boolean canImport(ContentType contentType) {
        return ContentType.DITA == contentType;
    }

    @Override
    public Resource importResource(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references) throws BlackbirdServiceException {
        try {
            return ditaCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent, references);
        } catch (CopyMergeDitaValidationException | BlackbirdResourceCopyMergeException ex) {
            throw mapException(ex, sourcePath, targetPath);
        }
    }

    @Override
    public Resource importResource(String sourcePath, String targetPath, String targetContent) throws BlackbirdServiceException {
        try {
            return ditaCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent);
        } catch (CopyMergeDitaValidationException | BlackbirdResourceCopyMergeException ex) {
            throw mapException(ex, sourcePath, targetPath);
        }
    }

    private BlackbirdServiceException mapException(Exception ex, String sourcePath, String targetPath) {
        if (ex instanceof CopyMergeDitaValidationException) {
            return new BlackbirdServiceException(String.format("Can not import dita file, folder for translated content must be named with the language code, targetPath: %s", targetPath), ex);
        }
        if (ex instanceof BlackbirdResourceCopyMergeException) {
            return new BlackbirdServiceException(String.format("Can not import dita file, sourcePath: %s, targetPath: %s", sourcePath, targetPath), ex);
        }
        return new BlackbirdServiceException(String.format("Unexpected error while importing dita file, sourcePath: %s, targetPath: %s", sourcePath, targetPath), ex);
    }
}
