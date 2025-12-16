package io.blackbird.aemconnector.core.services.impl.exporters.internal;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.constants.ServletConstants;
import io.blackbird.aemconnector.core.dto.v2.ContentReference;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.services.v2.ReferenceCollectorService;
import io.blackbird.aemconnector.core.utils.Node2JsonUtil;
import io.blackbird.aemconnector.core.utils.PathUtils;
import org.apache.sling.api.resource.Resource;

import javax.jcr.Node;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.blackbird.aemconnector.core.utils.jackson.JsonUtils.removeEmptyObjects;

public class AbstractPageExporter {

    public static final String STRUCTURE_JCR_CONTENT = "/structure/jcr:content";
    public static final String REFERENCES_PROP_NAME = "references";

    private final ReferenceCollectorService referenceCollectorService;
    private final TranslationRulesService translationRulesService;

    public AbstractPageExporter(ReferenceCollectorService referenceCollectorService, TranslationRulesService translationRulesService) {
        this.referenceCollectorService = referenceCollectorService;
        this.translationRulesService = translationRulesService;
    }

    protected Serializable exportContent(Resource resource) throws BlackbirdInternalErrorException {
        List<ContentReference> references = new ArrayList<>();
        collectPageReferences(resource, references);
        collectTemplateReferences(resource, references);

        ObjectNode jsonNode = Node2JsonUtil.serializeRecursively(resource.adaptTo(Node.class), translationRulesService, false);
        jsonNode.set(REFERENCES_PROP_NAME, Node2JsonUtil.getMapper().valueToTree(references));

        return jsonNode;
    }

    protected Serializable exportContent(Resource resource, Map<String, Object> options) throws BlackbirdInternalErrorException {
        List<ContentReference> references = new ArrayList<>();
        collectPageReferences(resource, references);
        collectTemplateReferences(resource, references);
        boolean ignoreTranslationRules = options.containsKey(ServletConstants.IGNORE_TRANSLATION_RULES);
        boolean isLiveCopy = options.containsKey(ServletConstants.CHECK_LIVE_COPY);
        Node node = resource.adaptTo(Node.class);
        ObjectNode jsonNode = ignoreTranslationRules
                ? Node2JsonUtil.serializeRecursively(node, isLiveCopy)
                : serializeWithTranslationRules(node, isLiveCopy);
        jsonNode.set(REFERENCES_PROP_NAME, Node2JsonUtil.getMapper().valueToTree(references));

        return jsonNode;
    }

    private ObjectNode serializeWithTranslationRules(Node node, boolean isLiveCopy) throws BlackbirdInternalErrorException {
        ObjectNode jsonNode = Node2JsonUtil.serializeRecursively(node, translationRulesService, isLiveCopy);
        removeEmptyObjects(jsonNode);
        return jsonNode;
    }

    protected void collectPageReferences(Resource resource, List<ContentReference> references) {
        String resourcePath = resource.getPath();
        referenceCollectorService.getReferences(resource)
                .stream()
                .map(contentReference -> stripParentFromPropertyPath(contentReference, resourcePath))
                .forEach(references::add);
    }

    protected void collectTemplateReferences(Resource resource, List<ContentReference> references) {
        getPageTemplateResource(resource)
                .map(referenceCollectorService::getReferences)
                .map(this::removeRedundantProperties)
                .ifPresent(references::addAll);
    }

    protected Optional<Resource> getPageTemplateResource(Resource pageResource) {
        return Optional.ofNullable(pageResource.adaptTo(Page.class))
                .map(Page::getTemplate)
                .map(Template::getPath)
                .map(path -> path + STRUCTURE_JCR_CONTENT)
                .map(pageResource.getResourceResolver()::getResource);

    }

    private List<ContentReference> removeRedundantProperties(List<ContentReference> references) {
        return references.stream().map(item ->
                        new ContentReference(item.getReferencePath()))
                .collect(Collectors.toList());
    }

    private ContentReference stripParentFromPropertyPath(ContentReference contentReference, String parentPath) {
        return new ContentReference(
                contentReference.getPropertyName(),
                PathUtils.stripParent(contentReference.getPropertyPath(), parentPath),
                contentReference.getReferencePath());

    }
}
