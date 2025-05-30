package io.blackbird.aemconnector.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import org.apache.sling.api.resource.Resource;

public interface ContentImporter {

    boolean canImport(ContentType contentType);

    Resource importResource(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references) throws BlackbirdServiceException;
}
