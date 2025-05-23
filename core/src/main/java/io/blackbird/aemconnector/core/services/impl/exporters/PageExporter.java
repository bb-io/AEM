package io.blackbird.aemconnector.core.services.impl.exporters;

import io.blackbird.aemconnector.core.services.ContentExporter;
import io.blackbird.aemconnector.core.services.ContentType;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.io.Serializable;

@Component(service = ContentExporter.class)
public class PageExporter implements ContentExporter {
    @Override
    public boolean canExport(ContentType contentType) {
        return ContentType.PAGE == contentType;
    }

    @Override
    public Serializable export(Resource resource) {
        return null;
    }
}
