package io.blackbird.aemconnector.core.servlets;

import com.day.cq.wcm.api.Page;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.objects.CqPageResource;
import io.blackbird.aemconnector.core.services.BlackbirdPageContentFilterService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import io.blackbird.aemconnector.core.utils.ResourceJsonUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Set;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = BlackbirdPageExportServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_GET
)
public class BlackbirdPageExportServlet extends BlackbirdAbstractBaseServlet {
    public static final String RESOURCE_TYPE = "bb-aem-connector/services/page-exporter";
    public static final String PAGE_PATH_PARAM = "pagePath";

    @Reference
    private BlackbirdPageContentFilterService blackbirdPageContentFilterService;

    @Override
    public Serializable getSerializableObject(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        String pagePath = request.getParameter(PAGE_PATH_PARAM);

        Resource resource = getResource(request, pagePath);

        Page page = resource.adaptTo(Page.class);

        if (page == null) {
            throw new BlackbirdHttpErrorException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Bad Request", String.format("'%s' is not a cq Page", pagePath));
        }

        Resource jcrContent = page.getContentResource();

        if (jcrContent == null) {
            throw new BlackbirdHttpErrorException(
                    HttpServletResponse.SC_CONFLICT,
                    "Conflict", String.format("Page exists but has no jcr:content node. Path: %s", pagePath));
        }

        Set<String> blacklistedPropertyNames = blackbirdPageContentFilterService.getBlacklistedPropertyNames();
        Set<String> blacklistedNodeNames = blackbirdPageContentFilterService.getBlacklistedNodeNames();
        return ResourceJsonUtil.serializeRecursively(new CqPageResource(resource),
                (p) -> !blacklistedPropertyNames.contains(p),
                (n) -> !blacklistedNodeNames.contains(n));
    }

    private static Resource getResource(SlingHttpServletRequest request, String pagePath) throws BlackbirdHttpErrorException {
        if (pagePath == null) {
            throw new BlackbirdHttpErrorException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Bad Request", String.format("Missing required query parameter: '%s'", PAGE_PATH_PARAM));
        }

        if (!pagePath.startsWith("/")) {
            throw new BlackbirdHttpErrorException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Bad Request", "pagePath must be an absolute JCR path starting with '/'"
            );
        }

        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource resource = resourceResolver.getResource(pagePath);

        if (resource == null) {
            throw new BlackbirdHttpErrorException(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Not Found", String.format("'%s' was not found", pagePath));
        }
        return resource;
    }
}
