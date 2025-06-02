package io.blackbird.aemconnector.core.services.impl.importers;

import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentImporter;
import io.blackbird.aemconnector.core.services.ContentType;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

@Component(service = ContentImporter.class)
public class ContentFragmentImporter implements ContentImporter {

    @Override
    public boolean canImport(ContentType contentType) {
        return ContentType.CONTENT_FRAGMENT == contentType;
    }

    @Override
    public Resource importResource(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references) throws BlackbirdServiceException {
        throw new UnsupportedOperationException("Import for Content Fragment not yet implemented");
    }
}
