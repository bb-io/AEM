package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import org.apache.sling.api.resource.Resource;

import java.util.Set;

public interface UpdateTagsService {

    Resource updateTags(String contentPath, Set<String> tagsToAdd, Set<String> tagsToRemove, ContentType contentType) throws BlackbirdServiceException;

}
