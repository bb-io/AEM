package io.blackbird.aemconnector.core.filters;

import io.blackbird.aemconnector.core.dto.BlackbirdErrorResponse;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.engine.EngineConstants;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.post.JSONResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.component.propertytypes.ServiceVendor;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.Serializable;

@Component(service = Filter.class,
        property = {
                EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_REQUEST,
        })
@ServiceDescription("Centralized Error Handling")
@ServiceRanking(1000)
@ServiceVendor("BlackBird")
@SlingServletFilter(pattern = "^/content/services/bb-aem-connector(/.*)?$")
public class GlobalExceptionHandlingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {

            filterChain.doFilter(servletRequest, servletResponse);

        } catch (RuntimeException e) {
            writeInternalErrorResponse(servletRequest, servletResponse, e);
        }
    }

    private void writeInternalErrorResponse(ServletRequest servletRequest, ServletResponse servletResponse, Exception e) throws IOException {
        final SlingHttpServletRequest request = (SlingHttpServletRequest) servletRequest;
        final SlingHttpServletResponse response = (SlingHttpServletResponse) servletResponse;

        configureResponseHeaders(response);

        BlackbirdErrorResponse errorResponse = BlackbirdErrorResponse.builder()
                .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .error("Internal Error")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        response.setStatus(errorResponse.getStatus());
        writeJsonResponse(response, errorResponse);
    }

    private void configureResponseHeaders(SlingHttpServletResponse response) {
        response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store");

    }

    private void writeJsonResponse(SlingHttpServletResponse response, Serializable payload) throws IOException {
        response.getWriter().write(BlackbirdAbstractBaseServlet.getObjectMapper().writeValueAsString(payload));
    }
}
