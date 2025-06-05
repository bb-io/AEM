package io.blackbird.aemconnector.core.services;

import com.day.cq.wcm.api.Page;
import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;

public interface BlackbirdPageCopyMergeService {
    Page copyAndMerge(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references) throws BlackbirdResourceCopyMergeException;
}
