package io.blackbird.aemconnector.core.filters.componentwrapper;

import io.blackbird.aemconnector.core.objects.TranslatableContent;
import io.blackbird.aemconnector.core.services.TranslatableDataExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.engine.EngineConstants;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceVendor;

import javax.jcr.Node;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component(service = Filter.class, property = {
        EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_COMPONENT,
})
@SlingServletFilter(pattern = "/content/.*\\.preview\\.html")
@ServiceDescription("Component wrapper filter that adds div wrapper with attributes to components to be translated")
@ServiceVendor("Blackbird.io")
public class ComponentWrapperFilter implements Filter {

    @Reference
    private TranslatableDataExtractor dataExtractor;

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {

        if (!(request instanceof SlingHttpServletRequest) || !(response instanceof SlingHttpServletResponse)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (response.isCommitted()) {
            filterChain.doFilter(request, response);
            return;
        }

        final SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;
        final ResponseContentCatcher contentCatcher = new ResponseContentCatcher(slingResponse);

        filterChain.doFilter(request, contentCatcher);

        if (!contentCatcher.hasContent()) {
            filterChain.doFilter(request, response);
            return;
        }

        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        final Resource resource = slingRequest.getResource();
        final String path = resource.getPath();
        final Node node = resource.adaptTo(Node.class);

        Map<String, TranslatableContent> translatableData = node == null
                ? Collections.emptyMap()
                : dataExtractor.extractFor(node);

        String componentContent = contentCatcher.getContent();
        TranslatableContent translatableContent = translatableData.get(path);

        final String output = translatableContent == null
                ? componentContent
                : new ComponentWrapper(translatableContent, componentContent).wrapComponent();

        slingResponse.getWriter().write(output);
    }
}

