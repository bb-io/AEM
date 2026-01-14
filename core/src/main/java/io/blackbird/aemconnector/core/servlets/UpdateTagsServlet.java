package io.blackbird.aemconnector.core.servlets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeService;
import io.blackbird.aemconnector.core.services.TagsValidationService;
import io.blackbird.aemconnector.core.services.UpdateTagsService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import io.blackbird.aemconnector.core.utils.ServletParameterHelper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = UpdateTagsServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_POST
)
public class UpdateTagsServlet extends BlackbirdAbstractBaseServlet {

    public static final String RESOURCE_TYPE = "bb-aem-connector/services/update-tags";

    private static final String CONTENT_PATH = "contentPath";
    private static final String ADD_TAGS = "addTags";
    private static final String REMOVE_TAGS = "removeTags";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Reference
    private transient ContentTypeService contentTypeService;

    @Reference
    private transient UpdateTagsService updateTagsService;

    @Reference
    private transient TagsValidationService tagsValidationService;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        JsonNode payload = ServletParameterHelper.getRequestPayload(request);
        String contentPath = payload.path(CONTENT_PATH).asText(null);
        Set<String> tagsToAdd = readStringSet(payload, ADD_TAGS);
        Set<String> tagsToRemove = readStringSet(payload, REMOVE_TAGS);
        validateParams(contentPath, tagsToAdd, tagsToRemove);
        Stream.of(tagsToAdd, tagsToRemove).filter(tags -> !tags.isEmpty()).forEach(tagsValidationService::validateTags);
        ObjectNode node = objectMapper.createObjectNode();
        try {
            ContentType contentType = contentTypeService.resolveContentType(contentPath);
            Resource resource = updateTagsService.updateTags(contentPath, tagsToAdd, tagsToRemove, contentType);
            node.put("message", "Tags updated successfully");
            node.put("path", resource.getPath());
            return node;
        } catch (BlackbirdServiceException ex) {
            throw BlackbirdHttpErrorException.internalServerError(ex.getMessage());
        }
    }

    private Set<String> readStringSet(JsonNode json, String fieldName) {
        JsonNode node = json.get(fieldName);
        if (node == null || !node.isArray()) {
            return Collections.emptySet();
        }
        return StreamSupport.stream(node.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toSet());
    }

    private void validateParams(String contentPath, Set<String> tagsToAdd, Set<String> tagsToRemove) throws BlackbirdHttpErrorException {
        if (contentPath == null) {
            throw BlackbirdHttpErrorException.badRequest(
                    String.format("Missing required field: '%s'", CONTENT_PATH));
        }
        if (tagsToAdd.isEmpty() && tagsToRemove.isEmpty()) {
            throw BlackbirdHttpErrorException.badRequest(
                    String.format("One of the required tag fields is missing: '%s' or '%s'", ADD_TAGS, REMOVE_TAGS));
        }
        Set<String> intersection = new HashSet<>(tagsToAdd);
        intersection.retainAll(tagsToRemove);
        if (!intersection.isEmpty()) {
            throw BlackbirdHttpErrorException.badRequest(
                    String.format("Conflicting tags found in both '%s' and '%s' values: %s", ADD_TAGS, REMOVE_TAGS, intersection));
        }
    }
}
