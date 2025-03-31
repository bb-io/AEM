package io.blackbird.aemconnector.core.servlets.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.blackbird.aemconnector.core.dto.BlackbirdErrorResponse;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
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
        try {
            if (validRequest) {
                Serializable serializable = this.getSerializableObject(request, response);
                this.doGet(request, response, serializable);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (BlackbirdHttpErrorException e) {
            sendError(response, BlackbirdErrorResponse.builder()
                    .status(e.getStatus())
                    .error(e.getError())
                    .message(e.getMessage())
                    .path(request.getRequestURI())
                    .build());
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

    private void sendError(SlingHttpServletResponse response, BlackbirdErrorResponse errorResponse) throws IOException {
        response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
        response.setStatus(errorResponse.getStatus());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    abstract public Serializable getSerializableObject(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException;
}
