package io.blackbird.aemconnector.core.services;

import org.apache.sling.api.resource.Resource;

public interface ContentTypeDetector {

    /**
     * Does this detector recognize this resource?
     */
    boolean detects(Resource resource);

    ContentType getContentType();
}
