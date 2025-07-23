package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.dto.BlackbirdPageEventSearchResult;
import io.blackbird.aemconnector.core.dto.BlackbirdPageEventViewerDto;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.objects.PageEventSearchParams;
import io.blackbird.aemconnector.core.services.BlackbirdPageEventService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = BlackbirdPageEventViewerServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_GET
)
public class BlackbirdPageEventViewerServlet extends BlackbirdAbstractBaseServlet {
    public static final String RESOURCE_TYPE = "bb-aem-connector/services/pages-events";
    public static final String ROOT_PATH = "rootPath";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String OFFSET = "offset";
    public static final String LIMIT = "limit";
    public static final String EVENTS = "events";
    public static final String TAGS = "tags";
    public static final String TYPE = "type";
    public static final String KEY_WORD = "keyword";

    private static final Set<String> ALLOWED_TYPES = Set.of("cq:Page", "dam:Asset", "nt:file");

    @Reference
    private BlackbirdPageEventService blackbirdPageEventService;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {

        String rootPath = ObjectUtils.defaultIfNull(request.getParameter(ROOT_PATH), "/");
        String type = resolveType(request.getParameter(TYPE));
        String startDate = request.getParameter(START_DATE);
        String endDate = request.getParameter(END_DATE);
        long offset = parseLongOrDefault(request.getParameter(OFFSET), 0);
        long limit = parseLongOrDefault(request.getParameter(LIMIT), -1);
        Set<String> events = getSetParams(request.getParameterValues(EVENTS));
        Set<String> tags = getSetParams(request.getParameterValues(TAGS));
        String keyword = request.getParameter(KEY_WORD);

        BlackbirdPageEventSearchResult searchResult;
        try {
            searchResult = blackbirdPageEventService.searchPageEvents(PageEventSearchParams.builder()
                    .rootPath(rootPath)
                    .type(type)
                    .startDate(startDate)
                    .endDate(endDate)
                    .events(events)
                    .tags(tags)
                    .keyword(keyword)
                    .offset(offset)
                    .limit(limit)
                    .build());
        } catch (LoginException e) {
            throw BlackbirdHttpErrorException.unauthorized(e.getMessage());
        } catch (BlackbirdInternalErrorException e) {
            throw BlackbirdHttpErrorException.internalServerError(e.getMessage());
        }

        return BlackbirdPageEventViewerDto.builder()
                .rootPath(rootPath)
                .type(type)
                .startDate(startDate)
                .endDate(endDate)
                .offset(offset)
                .limit(limit)
                .events(events)
                .tags(tags)
                .keyword(keyword)
                .totalMatches(searchResult.getTotalMatches())
                .hasMore(searchResult.isHasMore())
                .results(searchResult.getResults())
                .content(searchResult.getContent())
                .build();
    }

    private static String resolveType(String type) {
        return (type != null && ALLOWED_TYPES.contains(type)) ? type : "cq:Page";
    }

    private long parseLongOrDefault(String number, long defaultValue) {
        try {
            long parsedNumber = Long.parseLong(number);
            return parsedNumber > 0 ? parsedNumber : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Set<String> getSetParams(String[] params) {
        return Optional.ofNullable(params)
                .map(Arrays::asList)
                .orElse(Collections.emptyList()).stream()
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet());
    }
}
