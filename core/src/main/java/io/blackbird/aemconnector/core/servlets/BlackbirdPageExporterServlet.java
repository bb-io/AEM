package io.blackbird.aemconnector.core.servlets;

import com.day.cq.wcm.api.Page;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.objects.CqPageResource;
import io.blackbird.aemconnector.core.services.BlackbirdPageContentFilterService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import io.blackbird.aemconnector.core.utils.ResourceJsonUtil;
import io.blackbird.aemconnector.core.utils.ServletParameterHelper;
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
import java.util.Optional;
import java.util.Set;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = BlackbirdPageExporterServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_GET
)
public class BlackbirdPageExporterServlet extends BlackbirdAbstractBaseServlet {
    public static final String RESOURCE_TYPE = "bb-aem-connector/services/page-exporter";

    @Reference
    private BlackbirdPageContentFilterService blackbirdPageContentFilterService;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        String pagePath = ServletParameterHelper.getRequiredPagePath(request);

        Resource resource = getPagePathResource(request, pagePath);

        Page page = Optional.ofNullable(resource.adaptTo(Page.class)).orElseThrow(() -> new BlackbirdHttpErrorException(
                HttpServletResponse.SC_BAD_REQUEST,
                "Bad Request", String.format("'%s' is not a cq Page", pagePath)));

        Optional.ofNullable(page.getContentResource()).orElseThrow(() -> new BlackbirdHttpErrorException(
                HttpServletResponse.SC_CONFLICT,
                "Conflict", String.format("Page exists but has no jcr:content node. Path: %s", pagePath)));

        Set<String> blacklistedPropertyNames = blackbirdPageContentFilterService.getBlacklistedPropertyNames();
        Set<String> blacklistedNodeNames = blackbirdPageContentFilterService.getBlacklistedNodeNames();
        return ResourceJsonUtil.serializeRecursively(new CqPageResource(resource),
                (p) -> !blacklistedPropertyNames.contains(p),
                (n) -> !blacklistedNodeNames.contains(n));
    }

    private static Resource getPagePathResource(SlingHttpServletRequest request, String pagePath) throws BlackbirdHttpErrorException {
        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource resource = resourceResolver.getResource(pagePath);

        return Optional.ofNullable(resource).orElseThrow(() -> new BlackbirdHttpErrorException(
                HttpServletResponse.SC_NOT_FOUND,
                "Not Found", String.format("'%s' was not found", pagePath)));
    }
}
