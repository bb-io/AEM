package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.ContentExportService;
import io.blackbird.aemconnector.core.services.ContentExporter;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
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
    public Serializable exportContent(String path, ContentType contentType) throws BlackbirdServiceException {
        ContentExporter exporter = exporters.stream()
                .filter(e -> e.canExport(contentType))
                .findFirst()
                .orElseThrow(() -> new BlackbirdServiceException(String.format(
                        "No exporter for content type: %s at %s", contentType, path)));

        try (ResourceResolver resolver = serviceUserResolverProvider.getContentStructureReaderResolver()) {

            Resource resource = ObjectUtils.ensureNotNull(resolver.getResource(path),
                    () -> new BlackbirdServiceException(String.format(
                            "No resource found at path: %s", path)));

            Serializable result = exporter.export(resource);

            log.info("Export successful for path: {}, content type: {}", path, contentType);

            return result;

        } catch (LoginException e) {
            throw new BlackbirdServiceException(e.getMessage(), e);
        }
    }
}
