package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.ContentExportService;
import io.blackbird.aemconnector.core.services.ContentExporter;
import io.blackbird.aemconnector.core.services.ContentType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component(service = ContentExportService.class, immediate = true)
public class ContentExportServiceImpl implements ContentExportService {

    @Reference
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Reference(
            service = ContentExporter.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            bind = "bindExporter",
            unbind = "unbindExporter"
    )
    private final Set<ContentExporter> exporters = new CopyOnWriteArraySet<>();

    protected void bindExporter(ContentExporter exporter) {
        exporters.add(exporter);
    }

    protected void unbindExporter(ContentExporter exporter) {
        exporters.remove(exporter);
    }

    @Override
    public Serializable exportContent(String path, ContentType contentType) {
        return null;
    }
}
