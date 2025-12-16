package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.constants.ServletConstants;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentExportService;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import io.blackbird.aemconnector.core.utils.ServletParameterHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = DitaExporterServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_GET
)
public class DitaExporterServlet extends BlackbirdAbstractBaseServlet {
    public static final String RESOURCE_TYPE = "bb-aem-connector/services/dita-file-exporter";

    private static final String TYPE = "type";

    @Reference
    private transient ContentTypeService contentTypeService;

    @Reference
    private transient ContentExportService contentExportService;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        String contentPath = ServletParameterHelper.getRequiredContentPath(request);
        return export(contentPath, ServletParameterHelper.extractOptions(request));
    }

    @Override
    public InputStream buildXmlResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        String contentPath = ServletParameterHelper.getRequiredContentPath(request);
        Map<String, Object> options = ServletParameterHelper.extractOptions(request);
        options.put(TYPE, ServletConstants.XML_EXTENSION);
        Serializable result = export(contentPath, options);
        return new ByteArrayInputStream((String.valueOf(result)).getBytes(StandardCharsets.UTF_8));
    }

    private Serializable export(String contentPath, Map<String, Object> options) throws BlackbirdHttpErrorException {
        try {
            ContentType contentType = contentTypeService.resolveContentType(contentPath);
            Serializable result = contentExportService.exportContent(contentPath, contentType, options);

            log.info("Exported content for path: {}, content type: {}", contentPath, contentType);

            return result;
        } catch (BlackbirdServiceException e) {
            String message = e.getMessage();
            if (String.format("No resource found at path: %s", contentPath).equals(message)) {
                throw BlackbirdHttpErrorException.notFound(message);
            }
            throw BlackbirdHttpErrorException.internalServerError(message);
        }
    }
}