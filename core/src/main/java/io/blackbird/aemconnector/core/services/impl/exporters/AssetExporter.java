package io.blackbird.aemconnector.core.services.impl.exporters;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentExporter;
import io.blackbird.aemconnector.core.services.ContentType;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.io.Serializable;

@Component(service = ContentExporter.class)
public class AssetExporter implements ContentExporter {
    @Override
    public boolean canExport(ContentType contentType) {
        return ContentType.ASSET == contentType;
    }

    @Override
    public Serializable export(Resource resource) throws BlackbirdServiceException {
        throw new UnsupportedOperationException("Exporter for Asset not implemented");
    }
}
