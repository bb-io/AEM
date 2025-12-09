package io.blackbird.aemconnector.core.servlets.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.dto.BlackbirdErrorResponse;
import io.blackbird.aemconnector.core.dto.ErrorMessageXml;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.utils.ObjectUtils;
import io.blackbird.aemconnector.core.vo.BlackbirdRequestFilter;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.post.JSONResponse;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import static io.blackbird.aemconnector.core.constants.ServletConstants.APPLICATION_XML;
import static io.blackbird.aemconnector.core.constants.ServletConstants.UTF_8;
import static io.blackbird.aemconnector.core.constants.ServletConstants.XML_EXTENSION;
import static io.blackbird.aemconnector.core.constants.ServletConstants.XML_HANDLER_NOT_IMPLEMENTED_FOR_SERVLET_ERROR_MSG;

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
            String extension = request.getRequestPathInfo().getExtension();

            if (XML_EXTENSION.equals(extension)) {
                InputStream payload = buildXmlResponsePayload(request, response);
                writeXmlResponse(response, payload);

            } else {
                Serializable payload = buildResponsePayload(request, response);

                ObjectUtils.ensureNotNull(payload,
                        () -> BlackbirdHttpErrorException.notFound("No Content available"));

                configureResponseHeaders(response);
                writeJsonResponse(response, payload);
            }
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
        response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(payload));
    }

    private void writeXmlResponse(SlingHttpServletResponse response, InputStream payload) throws IOException {
        response.setContentType(APPLICATION_XML);
        response.setCharacterEncoding(UTF_8);
        IOUtils.copy(payload, response.getOutputStream());
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
        response.setCharacterEncoding(UTF_8);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store");
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        return defaultJsonResponse();
    }

    public InputStream buildXmlResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        try {
            return defaultXmlResponse();
        } catch (JAXBException e) {
            throw BlackbirdHttpErrorException.internalServerError(e.getMessage());
        }
    }

    private Serializable defaultJsonResponse() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("message", "JSON response handling is not implemented for this servlet");
        return node;
    }

    private InputStream defaultXmlResponse() throws JAXBException {
        ErrorMessageXml errorMsg = new ErrorMessageXml(XML_HANDLER_NOT_IMPLEMENTED_FOR_SERVLET_ERROR_MSG);
        JAXBContext context = JAXBContext.newInstance(ErrorMessageXml.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        marshaller.marshal(errorMsg, outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
