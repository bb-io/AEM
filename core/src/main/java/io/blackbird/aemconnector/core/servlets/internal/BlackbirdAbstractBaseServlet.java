package io.blackbird.aemconnector.core.servlets.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.blackbird.aemconnector.core.dto.BlackbirdErrorResponse;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.vo.BlackbirdRequestFilter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.post.JSONResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

public abstract class BlackbirdAbstractBaseServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        try {
            validateRequest(request);
            Serializable serializable = this.getSerializableObject(request, response);
            this.doGet(request, response, serializable);

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
        setResponseHeaders(response);
        if (serializable != null) {
            writeJsonResponse(response, serializable);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        try {
            validateRequest(request);
            Serializable serializableObject = getSerializableObject(request, response);
            writeJsonResponse(response, serializableObject);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (BlackbirdHttpErrorException e) {
            sendError(response, BlackbirdErrorResponse.builder()
                    .status(e.getStatus())
                    .error(e.getError())
                    .message(e.getMessage())
                    .path(request.getRequestURI())
                    .build());
        }
    }

    private void writeJsonResponse(SlingHttpServletResponse response, Serializable serializable) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        response.getWriter().write(objectMapper.writeValueAsString(serializable));
    }

    private void validateRequest(SlingHttpServletRequest request) throws BlackbirdHttpErrorException {
        boolean isValidRequest = readRequestFilter(request);
        if (!isValidRequest) {
            throw new BlackbirdHttpErrorException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Bad Request",
                    "Request rejected. It failed to meet the filtering criteria");
        }
    }

    private boolean readRequestFilter(SlingHttpServletRequest request) {
        BlackbirdRequestFilter requestFilter = new BlackbirdRequestFilter(request);
        return requestFilter.isValidFilters();
    }

    private void sendError(SlingHttpServletResponse response, BlackbirdErrorResponse errorResponse) throws IOException {
        setResponseHeaders(response);
        response.setStatus(errorResponse.getStatus());
        writeJsonResponse(response, errorResponse);
    }

    private void setResponseHeaders(SlingHttpServletResponse response) {
        response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-control", "no-cache, no-store");
    }

    abstract public Serializable getSerializableObject(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException;
}
