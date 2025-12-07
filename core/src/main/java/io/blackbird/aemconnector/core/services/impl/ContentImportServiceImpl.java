package io.blackbird.aemconnector.core.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentImportService;
import io.blackbird.aemconnector.core.services.ContentImporter;
import io.blackbird.aemconnector.core.services.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component(service = ContentImportService.class, immediate = true)
public class ContentImportServiceImpl implements ContentImportService {

    @Reference(
            service = ContentImporter.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            bind = "bindImporter",
            unbind = "unbindImporter"
    )
    private final Set<ContentImporter> importers = new CopyOnWriteArraySet<>();

    protected void bindImporter(ContentImporter importer) {
        importers.add(importer);
    }

    protected void unbindImporter(ContentImporter importer) {
        importers.remove(importer);
    }

    @Override
    public Resource importContent(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references, ContentType contentType) throws BlackbirdServiceException {
        ContentImporter importer = getImporter(contentType, sourcePath);

        Resource resource = importer.importResource(sourcePath, targetPath, targetContent, references);
        log.info("Import successful for path: {}, content type: {}, json format", resource.getPath(), contentType);
        return resource;
    }

    @Override
    public Resource importContent(String sourcePath, String targetPath, String targetContent, ContentType contentType) throws BlackbirdServiceException {
        ContentImporter importer = getImporter(contentType, sourcePath);

        Resource resource = importer.importResource(sourcePath, targetPath, targetContent);
        log.info("Import successful for path: {}, content type: {}, xml format", resource.getPath(), contentType);
        return resource;
    }

    private ContentImporter getImporter(ContentType contentType, String sourcePath) throws BlackbirdServiceException {
        return importers.stream()
                .filter(i -> i.canImport(contentType))
                .findFirst()
                .orElseThrow(() -> new BlackbirdServiceException(String.format("No importer for content type: %s at %s", contentType, sourcePath)));
    }
}
