package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.services.BlackbirdContentStructureService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
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
    public Serializable getSerializableObject(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        String suffix = request.getRequestPathInfo().getSuffix();
        if (StringUtils.isNotEmpty(suffix) && suffix.contains("/")) {
            return contentStructureService.getContentStructure(suffix);
        }
        return null;
    }
}
