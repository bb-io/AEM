package io.blackbird.aemconnector.core.servlets;

import com.day.cq.wcm.api.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdPageCopyMergeException;
import io.blackbird.aemconnector.core.services.BlackbirdPageCopyMergeService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = BlackbirdPageImporterServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_POST
)
public class BlackbirdPageImporterServlet extends BlackbirdAbstractBaseServlet {
    public static final String RESOURCE_TYPE = "bb-aem-connector/services/page-importer";
    public static final String SOURCE_PATH = "sourcePath";
    public static final String TARGET_PATH = "targetPath";
    public static final String TARGET_CONTENT = "targetContent";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Reference
    private transient BlackbirdPageCopyMergeService pageCopyMergeService;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {

        JsonNode payload = getRequestPayload(request);

        String sourcePath = payload.path(SOURCE_PATH).asText(null);
        String targetPath = payload.path(TARGET_PATH).asText(null);
        JsonNode targetContent = payload.path(TARGET_CONTENT);

        validateParams(sourcePath, targetPath, targetContent);
        ObjectNode node = objectMapper.createObjectNode();

        try {
            Page page = pageCopyMergeService.copyAndMerge(sourcePath, targetPath, targetContent);
            node.put("message", "Page imported successfully");
            node.put("path", page.getPath());
            return node;
        } catch (BlackbirdPageCopyMergeException e) {
            throw BlackbirdHttpErrorException.internalServerError(e.getMessage());
        } catch (LoginException e) {
            throw BlackbirdHttpErrorException.unauthorized(e.getMessage());
        }
    }

    private JsonNode getRequestPayload(SlingHttpServletRequest request) throws BlackbirdHttpErrorException {
        try {
            String requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
            return objectMapper.readTree(requestBody);
        } catch (IOException e) {
            throw BlackbirdHttpErrorException.internalServerError(e.getMessage());
        }
    }

    private void validateParams(String sourcePath, String targetPath, JsonNode targetContent) throws BlackbirdHttpErrorException {
        if (ObjectUtils.anyNull(sourcePath, targetPath) || targetContent.isMissingNode() || !targetContent.isObject()) {
            throw BlackbirdHttpErrorException.badRequest(
                    String.format("Missing required fields: %s, %s, or %s", SOURCE_PATH, TARGET_PATH, TARGET_CONTENT));
        }
    }
}
