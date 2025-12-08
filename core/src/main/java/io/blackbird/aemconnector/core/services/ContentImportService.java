package io.blackbird.aemconnector.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import org.apache.sling.api.resource.Resource;

public interface ContentImportService {

    Resource importContent(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references, ContentType contentType) throws BlackbirdServiceException;

    Resource importContent(String sourcePath, String targetPath, String targetContent, ContentType contentType) throws BlackbirdServiceException;
}
