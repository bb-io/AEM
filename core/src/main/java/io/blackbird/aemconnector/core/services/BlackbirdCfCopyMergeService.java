package io.blackbird.aemconnector.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import org.apache.sling.api.resource.Resource;

public interface BlackbirdCfCopyMergeService {

    Resource copyAndMerge(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references) throws BlackbirdResourceCopyMergeException;

}
