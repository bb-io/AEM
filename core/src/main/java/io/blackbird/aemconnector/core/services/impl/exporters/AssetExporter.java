package io.blackbird.aemconnector.core.services.impl.exporters;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.Map;

import static io.blackbird.aemconnector.core.utils.jackson.JsonUtils.removeEmptyObjects;

@Component(service = ContentExporter.class)
public class AssetExporter implements ContentExporter {

    @Reference
    private TranslationRulesService translationRulesService;

    @Override
    public boolean canExport(ContentType contentType) {
        return ContentType.ASSET == contentType;
    }

    @Override
    public Serializable export(Resource resource, Map<String, Object> options) throws BlackbirdServiceException {

        try {
            ObjectNode jsonNode = Node2JsonUtil.serializeRecursively(resource.adaptTo(Node.class), translationRulesService);
            removeEmptyObjects(jsonNode);
            return jsonNode;
        } catch (BlackbirdInternalErrorException e) {
            throw new BlackbirdServiceException(e.getMessage(), e);
        }
    }

}
