package io.blackbird.aemconnector.core.services.impl.exporters;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.dto.v2.ContentReference;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentExporter;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.services.v2.ReferenceCollectorService;
import io.blackbird.aemconnector.core.utils.Node2JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import java.io.Serializable;
import java.util.List;

@Slf4j
@Component(service = ContentExporter.class)
public class ContentFragmentExporter implements ContentExporter {

    @Reference
    private TranslationRulesService translationRulesService;

    @Reference
    private ReferenceCollectorService referenceCollectorService;

    @Override
    public boolean canExport(ContentType contentType) {
        return ContentType.CONTENT_FRAGMENT == contentType;
    }


    @Override
    public Serializable export(Resource resource) {

        try {
            Node jcrNode = resource.adaptTo(Node.class);
            ObjectNode jsonNode = Node2JsonUtil.serializeRecursively(jcrNode, translationRulesService);

            List<ContentReference> references = referenceCollectorService.getReferences(resource);

            jsonNode.set("references", Node2JsonUtil.getMapper().valueToTree(references));
            return jsonNode;
        } catch (BlackbirdInternalErrorException e) {
            throw new BlackbirdServiceException(e.getMessage(), e);
        }
    }
}
