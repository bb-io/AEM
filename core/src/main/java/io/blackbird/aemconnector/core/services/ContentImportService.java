package io.blackbird.aemconnector.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import org.apache.sling.api.resource.Resource;

public interface ContentImportService {

    Resource importContent(String sourcePath, String targetPath, JsonNode targetContent, ContentType contentType) throws BlackbirdServiceException;
}
