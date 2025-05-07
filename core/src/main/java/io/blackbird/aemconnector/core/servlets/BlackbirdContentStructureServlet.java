package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.models.BlackbirdContentStructureModel;
import io.blackbird.aemconnector.core.services.BlackbirdContentStructureService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import io.blackbird.aemconnector.core.utils.ObjectUtils;
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
        resourceTypes = BlackbirdContentStructureServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_GET
)
public class BlackbirdContentStructureServlet extends BlackbirdAbstractBaseServlet {
    public static final String RESOURCE_TYPE = "bb-aem-connector/services/content";

    @Reference
    private transient BlackbirdContentStructureService contentStructureService;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        String contentPath = ServletParameterHelper.getRequiredContentPath(request);
        BlackbirdContentStructureModel contentStructure = contentStructureService.getContentStructure(contentPath);

        return ObjectUtils.ensureNotNull(contentStructure,
                () -> BlackbirdHttpErrorException.notFound(
                        String.format("Content Not found for path: %s", contentPath)));
    }
}
