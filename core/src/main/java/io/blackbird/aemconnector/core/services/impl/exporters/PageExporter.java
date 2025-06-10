package io.blackbird.aemconnector.core.services.impl.exporters;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentExporter;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.services.impl.exporters.internal.AbstractPageExporter;
import io.blackbird.aemconnector.core.services.v2.ReferenceCollectorService;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.Serializable;
import java.util.Map;

@Component(service = ContentExporter.class)
public class PageExporter extends AbstractPageExporter implements ContentExporter {

    @Activate
    public PageExporter(@Reference ReferenceCollectorService referenceCollectorService,
                        @Reference TranslationRulesService translationRulesService) {
        super(referenceCollectorService, translationRulesService);
    }

    @Override
    public boolean canExport(ContentType contentType) {
        return ContentType.PAGE == contentType;
    }

    @Override
    public Serializable export(Resource resource, Map<String, Object> options) throws BlackbirdServiceException {
        try {
            return exportContent(resource);
        } catch (BlackbirdInternalErrorException e) {
            throw new BlackbirdServiceException(e.getMessage(), e);
        }
    }
}
