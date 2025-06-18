package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import org.apache.sling.api.resource.Resource;

import java.io.Serializable;
import java.util.Map;

public interface ContentExporter {

    boolean canExport(ContentType contentType);

    Serializable export(Resource resource, Map<String, Object> options) throws BlackbirdServiceException;
}
