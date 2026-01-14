package io.blackbird.aemconnector.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static io.blackbird.aemconnector.core.constants.ServletConstants.CONTENT_PATH_PARAM;
import static io.blackbird.aemconnector.core.constants.ServletConstants.PAGE_PATH_PARAM;
import static io.blackbird.aemconnector.core.constants.ServletConstants.SOURCE_PATH_PARAM;
import static io.blackbird.aemconnector.core.constants.ServletConstants.TARGET_PATH_PARAM;

public final class ServletParameterHelper {

    public static String getRequiredPagePath(SlingHttpServletRequest request) throws BlackbirdHttpErrorException {
        return getRequiredAbsolutePath(request, PAGE_PATH_PARAM);
    }

    public static String getRequiredContentPath(SlingHttpServletRequest request) throws BlackbirdHttpErrorException {
        return getRequiredAbsolutePath(request, CONTENT_PATH_PARAM);
    }

    public static String getRequiredSourcePath(SlingHttpServletRequest request) throws BlackbirdHttpErrorException {
        return getRequiredAbsolutePath(request, SOURCE_PATH_PARAM);
    }

    public static String getRequiredTargetPath(SlingHttpServletRequest request) throws BlackbirdHttpErrorException {
        return getRequiredAbsolutePath(request, TARGET_PATH_PARAM);
    }

    public static Map<String, Object> extractOptions(SlingHttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    String[] values = entry.getValue();
                    return values.length == 1
                            ? values[0]
                            : Arrays.asList(values);
                }));
    }

    public static JsonNode getRequestPayload(SlingHttpServletRequest request) throws BlackbirdHttpErrorException {
        try {
            String requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
            return new ObjectMapper().readTree(requestBody);
        } catch (IOException e) {
            throw BlackbirdHttpErrorException.internalServerError(e.getMessage());
        }
    }

    private static String getRequiredAbsolutePath(SlingHttpServletRequest request, String parameterName) throws BlackbirdHttpErrorException {
        String value = request.getParameter(parameterName);
        if (StringUtils.isEmpty(value)) {
            throw new BlackbirdHttpErrorException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Bad Request", String.format("Missing required query parameter: '%s'", parameterName));
        }

        if (!value.startsWith("/")) {
            throw new BlackbirdHttpErrorException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Bad Request", String.format("%s must be an absolute JCR path starting with '/'", parameterName)
            );
        }
        return value;
    }
}
