package io.blackbird.aemconnector.core.servlets;

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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = ContentExporterServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_GET
)
public class ContentExporterServlet extends BlackbirdAbstractBaseServlet {
    public static final String RESOURCE_TYPE = "bb-aem-connector/services/content-exporter";

    @Reference
    private transient ContentTypeService contentTypeService;

    @Reference
    private transient ContentExportService contentExportService;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        String contentPath = ServletParameterHelper.getRequiredContentPath(request);
        Map<String, Object> options = extractOptions(request);

        try {
            ContentType contentType = contentTypeService.resolveContentType(contentPath);
            Serializable result = contentExportService.exportContent(contentPath, contentType, options);

            log.info("Exported content for path: {}, content type: {}", contentPath, contentType);

            return result;
        } catch (BlackbirdServiceException e) {
            throw BlackbirdHttpErrorException.internalServerError(e.getMessage());
        }
    }

    private static Map<String, Object> extractOptions(SlingHttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    String[] values = entry.getValue();
                    return values.length == 1
                            ? values[0]
                            : Arrays.asList(values);
                }));
    }


}
