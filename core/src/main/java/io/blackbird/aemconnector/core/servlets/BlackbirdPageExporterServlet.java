package io.blackbird.aemconnector.core.servlets;

import com.day.cq.wcm.api.Page;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import io.blackbird.aemconnector.core.utils.Node2JsonUtil;
import io.blackbird.aemconnector.core.utils.ObjectUtils;
import io.blackbird.aemconnector.core.utils.ServletParameterHelper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.servlet.Servlet;
import java.io.Serializable;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = BlackbirdPageExporterServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_GET
)
public class BlackbirdPageExporterServlet extends BlackbirdAbstractBaseServlet {
    public static final String RESOURCE_TYPE = "bb-aem-connector/services/page-exporter";

    @Reference
    private TranslationRulesService translationRulesService;

    @Reference
    private BlackbirdServiceUserResolverProvider resolverProvider;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        String pagePath = ServletParameterHelper.getRequiredPagePath(request);

        try (ResourceResolver resourceResolver = resolverProvider.getPageContentReaderResolver()) {

            Resource pageContentResource = getPageResourceByPath(pagePath, resourceResolver);

            return Node2JsonUtil.serializeRecursively(pageContentResource.adaptTo(Node.class), translationRulesService);
        } catch (LoginException e) {
            throw BlackbirdHttpErrorException.unauthorized(e.getMessage());
        } catch (BlackbirdInternalErrorException e) {
            throw BlackbirdHttpErrorException.internalServerError(e.getMessage());
        }
    }

    private Resource getPageResourceByPath(String pagePath, ResourceResolver resourceResolver) throws BlackbirdHttpErrorException {

        Resource resource = ObjectUtils.ensureNotNull(
                resourceResolver.getResource(pagePath),
                () -> BlackbirdHttpErrorException.notFound(
                        String.format("'%s' was not found", pagePath)));

        Page page = ObjectUtils.ensureNotNull(
                resource.adaptTo(Page.class),
                () -> BlackbirdHttpErrorException.badRequest(
                        String.format("'%s' is not a cq Page", pagePath)));

        ObjectUtils.ensureNotNull(
                page.getContentResource(),
                () -> BlackbirdHttpErrorException.conflict(
                        String.format("Page exists but has no jcr:content node. Path: %s", pagePath)));

        return resource;
    }
}
