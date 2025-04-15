package io.blackbird.aemconnector.core.utils;

import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import static io.blackbird.aemconnector.core.constants.ServletConstants.CONTENT_PATH_PARAM;
import static io.blackbird.aemconnector.core.constants.ServletConstants.PAGE_PATH_PARAM;

public final class ServletParameterHelper {

    public static String getRequiredPagePath(SlingHttpServletRequest request) throws BlackbirdHttpErrorException {
        return getRequiredAbsolutePath(request, PAGE_PATH_PARAM);
    }

    public static String getRequiredContentPath(SlingHttpServletRequest request) throws BlackbirdHttpErrorException {
        return getRequiredAbsolutePath(request, CONTENT_PATH_PARAM);
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
