package io.blackbird.aemconnector.core.servlets.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.blackbird.aemconnector.core.vo.BlackbirdRequestFilter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.post.JSONResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

public abstract class BlackbirdAbstractBaseServlet extends SlingSafeMethodsServlet {
    public static final String BASE_SERVLET_PATH = "/services/bb-aem-connector";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        boolean validRequest = this.readRequestFilter(request);
        if (validRequest) {
            Serializable serializable = this.getSerializableObject(request, response);
            this.doGet(request, response, serializable);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response, Serializable serializable) throws IOException {
        if (serializable != null) {
            response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            response.getWriter().write(objectMapper.writeValueAsString(serializable));
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private boolean readRequestFilter(SlingHttpServletRequest request) {
        BlackbirdRequestFilter requestFilter = new BlackbirdRequestFilter(request);
        return requestFilter.isValidFilters();
    }

    abstract public Serializable getSerializableObject(SlingHttpServletRequest request, SlingHttpServletResponse response);
}
