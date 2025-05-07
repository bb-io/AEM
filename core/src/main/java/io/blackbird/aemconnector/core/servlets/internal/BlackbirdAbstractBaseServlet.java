package io.blackbird.aemconnector.core.servlets.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.blackbird.aemconnector.core.dto.BlackbirdErrorResponse;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.utils.ObjectUtils;
import io.blackbird.aemconnector.core.vo.BlackbirdRequestFilter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.post.JSONResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

public abstract class BlackbirdAbstractBaseServlet extends SlingAllMethodsServlet {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        handleRequest(request, response, HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        handleRequest(request, response, HttpServletResponse.SC_CREATED);
    }

    /**
     * Centralizes the handling logic for GET and POST requests.
     *
     * @param request       the Sling HTTP request
     * @param response      the Sling HTTP response
     * @param successStatusCode the HTTP status code to set upon successful handling
     * @throws IOException if an I/O error occurs
     */
    private void handleRequest(SlingHttpServletRequest request, SlingHttpServletResponse response, int successStatusCode) throws IOException {
        try {
            ensureValidRequest(request);
            Serializable payload = buildResponsePayload(request, response);

            ObjectUtils.ensureNotNull(payload,
                    () -> BlackbirdHttpErrorException.notFound("No Content available"));

            configureResponseHeaders(response);
            writeJsonResponse(response, payload);
            response.setStatus(successStatusCode);
        } catch (BlackbirdHttpErrorException e) {
            writeErrorResponse(response, BlackbirdErrorResponse.builder()
                    .status(e.getStatus())
                    .error(e.getError())
                    .message(e.getMessage())
                    .path(request.getRequestURI())
                    .build());
        }
    }

    private void writeJsonResponse(SlingHttpServletResponse response, Serializable payload) throws IOException {
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(payload));
    }

    private void ensureValidRequest(SlingHttpServletRequest request) throws BlackbirdHttpErrorException {
        boolean isValidRequest = readRequestFilter(request);
        if (!isValidRequest) {
            throw BlackbirdHttpErrorException.badRequest(
                    "Request rejected. It failed to meet the filtering criteria");
        }
    }

    private boolean readRequestFilter(SlingHttpServletRequest request) {
        BlackbirdRequestFilter requestFilter = new BlackbirdRequestFilter(request);
        return requestFilter.isValidFilters();
    }

    /**
     * Writes an error response in JSON format.
     *
     * @param response      the Sling HTTP response
     * @param errorResponse the error response details to send
     * @throws IOException if an I/O error occurs
     */
    private void writeErrorResponse(SlingHttpServletResponse response, BlackbirdErrorResponse errorResponse) throws IOException {
        configureResponseHeaders(response);
        response.setStatus(errorResponse.getStatus());
        writeJsonResponse(response, errorResponse);
    }

    private void configureResponseHeaders(SlingHttpServletResponse response) {
        response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-control", "no-cache, no-store");
    }

    abstract public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException;
}
