package io.blackbird.aemconnector.core.services;

import org.apache.sling.api.resource.Resource;

import java.io.Serializable;

public interface ContentExporter {

    boolean canExport(ContentType contentType);

    Serializable export(Resource resource);
}
