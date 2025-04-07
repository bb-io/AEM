package io.blackbird.aemconnector.core.services;

import com.day.cq.wcm.api.Page;
import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdPageCopyMergeException;
import org.apache.sling.api.resource.LoginException;

public interface BlackbirdPageCopyMergeService {
    Page copyAndMerge(String sourcePath, String targetPath, JsonNode targetContent) throws BlackbirdPageCopyMergeException, LoginException;
}
