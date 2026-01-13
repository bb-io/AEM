package io.blackbird.aemconnector.core.servlets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentImportService;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import io.blackbird.aemconnector.core.utils.ServletParameterHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.Serializable;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = ContentImporterServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_POST
)
public class ContentImporterServlet extends BlackbirdAbstractBaseServlet {

    public static final String RESOURCE_TYPE = "bb-aem-connector/services/content-importer";
    private static final String SOURCE_PATH = "sourcePath";
    private static final String TARGET_PATH = "targetPath";
    private static final String TARGET_CONTENT = "targetContent";
    private static final String REFERENCES = "references";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Reference
    private transient ContentTypeService contentTypeService;

    @Reference
    private transient ContentImportService contentImportService;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        JsonNode payload = ServletParameterHelper.getRequestPayload(request);

        String sourcePath = payload.path(SOURCE_PATH).asText(null);
        String targetPath = payload.path(TARGET_PATH).asText(null);
        JsonNode targetContent = payload.path(TARGET_CONTENT);
        JsonNode references = payload.path(REFERENCES);

        validateParams(sourcePath, targetPath, targetContent);
        ObjectNode node = objectMapper.createObjectNode();
        try {
            ContentType contentType = contentTypeService.resolveContentType(sourcePath);
            Resource resource = contentImportService.importContent(sourcePath, targetPath, targetContent, references, contentType);
            node.put("message", "Content imported successfully");
            node.put("path", resource.getPath());
            return node;
        } catch (BlackbirdServiceException ex) {
            throw BlackbirdHttpErrorException.internalServerError(ex.getMessage());
        }
    }

    private void validateParams(String sourcePath, String targetPath, JsonNode targetContent) throws BlackbirdHttpErrorException {
        if (ObjectUtils.anyNull(sourcePath, targetPath) || targetContent.isMissingNode() || !targetContent.isObject()) {
            throw BlackbirdHttpErrorException.badRequest(
                    String.format("Missing required fields: %s, %s, or %s", SOURCE_PATH, TARGET_PATH, TARGET_CONTENT));
        }
    }
}
