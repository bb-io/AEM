package io.blackbird.aemconnector.core.services.impl.exporters;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentExporter;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.utils.Node2JsonUtil;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import java.io.Serializable;

@Component(service = ContentExporter.class)
public class PageExporter implements ContentExporter {

    @Reference
    private TranslationRulesService translationRulesService;

    @Override
    public boolean canExport(ContentType contentType) {
        return ContentType.PAGE == contentType;
    }

    @Override
    public Serializable export(Resource resource) throws BlackbirdServiceException {
        try {
            return Node2JsonUtil.serializeRecursively(resource.adaptTo(Node.class), translationRulesService);
        } catch (BlackbirdInternalErrorException e) {
            throw new BlackbirdServiceException(e.getMessage(), e);
        }
    }
}
