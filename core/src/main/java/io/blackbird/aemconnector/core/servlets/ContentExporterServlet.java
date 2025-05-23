package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.services.ContentExportService;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import io.blackbird.aemconnector.core.utils.ServletParameterHelper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.Serializable;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = ContentExporterServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_GET
)
public class ContentExporterServlet extends BlackbirdAbstractBaseServlet {
    public static final String RESOURCE_TYPE = "bb-aem-connector/services/content-exporter";

    @Reference
    private ContentTypeService contentTypeService;

    @Reference
    private ContentExportService contentExportService;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        String contentPath = ServletParameterHelper.getRequiredContentPath(request);

        //resolve content type
        ContentType contentType = contentTypeService.resolveContentType(contentPath);

        //serialize content
        Serializable result = contentExportService.exportContent(contentPath, contentType);

        //return result

        return result;
    }
}
