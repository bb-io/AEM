package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.constants.ServletConstants;
import io.blackbird.aemconnector.core.dto.DitaFileImportResponse;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentImportService;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import io.blackbird.aemconnector.core.utils.ServletParameterHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
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
        resourceTypes = DitaImporterServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_POST,
        extensions = ServletConstants.XML
)
public class DitaImporterServlet extends BlackbirdAbstractBaseServlet {

    public static final String RESOURCE_TYPE = "bb-aem-connector/services/dita-file-importer";
    private static final String SOURCE_PATH = "sourcePath";
    private static final String TARGET_PATH = "targetPath";
    private static final String TARGET_CONTENT = "targetContent";

    @Reference
    private transient ContentTypeService contentTypeService;

    @Reference
    private transient ContentImportService contentImportService;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        String sourcePath = ServletParameterHelper.getRequiredSourcePath(request);
        String targetPath = ServletParameterHelper.getRequiredTargetPath(request);
        String targetContent = getRequestPayload(request);

        try {
            ContentType contentType = contentTypeService.resolveContentType(sourcePath);
            Resource resource = contentImportService.importContent(sourcePath, targetPath, targetContent, contentType);
            return new DitaFileImportResponse("Content imported successfully", resource.getPath()).toString();
        } catch (BlackbirdServiceException ex) {
            throw BlackbirdHttpErrorException.internalServerError(ex.getMessage());
        }
    }

    private String getRequestPayload(SlingHttpServletRequest request) throws BlackbirdHttpErrorException {
        try {
            String targetContent = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
            if (ObjectUtils.isEmpty(targetContent)) {
                throw BlackbirdHttpErrorException.badRequest("Request body is empty. Please add xml data for import.");
            }
            return targetContent;
        } catch (IOException e) {
            throw BlackbirdHttpErrorException.internalServerError(e.getMessage());
        }
    }
}
