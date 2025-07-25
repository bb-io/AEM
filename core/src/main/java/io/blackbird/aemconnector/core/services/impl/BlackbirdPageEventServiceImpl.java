package io.blackbird.aemconnector.core.services.impl;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import io.blackbird.aemconnector.core.dto.BlackbirdPageEventSearchResult;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.models.BlackbirdEventViewerContent;
import io.blackbird.aemconnector.core.objects.PageEventSearchParams;
import io.blackbird.aemconnector.core.services.BlackbirdPageEventService;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED;

@Slf4j
@Component(service = BlackbirdPageEventService.class, immediate = true)
public class BlackbirdPageEventServiceImpl implements BlackbirdPageEventService {

    public static final String CREATED = "created";
    public static final String MODIFIED = "modified";

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Override
    public BlackbirdPageEventSearchResult searchPageEvents(PageEventSearchParams params) throws LoginException, BlackbirdInternalErrorException {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("type", params.getType());
        queryMap.put("path", params.getRootPath());
        queryMap.put("p.offset", String.valueOf(params.getOffset()));
        queryMap.put("p.limit", String.valueOf(params.getLimit()));
        queryMap.put("p.guessTotal", "100");
        queryMap.put("group.p.or", "true");
        Optional.ofNullable(params.getKeyword()).ifPresent(keyword -> queryMap.put("fulltext", keyword));
        addTagPredicates(queryMap, params.getTags());
        Set<String> events = params.getEvents();

        boolean createdOnly = events.size() == 1 && events.contains(CREATED);
        boolean modifiedOnly = events.size() == 1 && events.contains(MODIFIED);

        if (createdOnly) {
            setDaterangePredicate(DaterangePredicateParams.builder()
                    .queryMap(queryMap)
                    .property(JCR_CREATED)
                    .prefix("group.1")
                    .lowerBoundDate(params.getStartDate())
                    .upperBoundDate(params.getEndDate())
                    .build());

        } else if (modifiedOnly) {
            setDaterangePredicate(DaterangePredicateParams.builder()
                    .queryMap(queryMap)
                    .property(JCR_CONTENT + "/cq:lastModified")
                    .prefix("group.1")
                    .lowerBoundDate(params.getStartDate())
                    .upperBoundDate(params.getEndDate())
                    .build());
        } else {
            setDaterangePredicate(DaterangePredicateParams.builder()
                    .queryMap(queryMap)
                    .property(JCR_CREATED)
                    .prefix("group.1")
                    .lowerBoundDate(params.getStartDate())
                    .upperBoundDate(params.getEndDate())
                    .build());

            setDaterangePredicate(DaterangePredicateParams.builder()
                    .queryMap(queryMap)
                    .property(JCR_CONTENT + "/cq:lastModified")
                    .prefix("group.2")
                    .lowerBoundDate(params.getStartDate())
                    .upperBoundDate(params.getEndDate())
                    .build());
        }

        queryMap.put("orderby", createdOnly
                ? "@jcr:created"
                : "@jcr:content/cq:lastModified");

        long totalMatches;
        boolean hasMore;
        List<BlackbirdEventViewerContent> content = new ArrayList<>();
        int results;

        try (ResourceResolver resourceResolver = serviceUserResolverProvider.getPageContentReaderResolver()) {
            Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), resourceResolver.adaptTo(Session.class));
            SearchResult result = query.getResult();
            totalMatches = result.getTotalMatches();
            hasMore = result.hasMore();

            List<Hit> hits = result.getHits();

            results = hits.size();

            for (Hit hit : hits) {
                Resource resource = hit.getResource();
                BlackbirdEventViewerContent blackbirdEventViewerContent = resource.adaptTo(BlackbirdEventViewerContent.class);
                content.add(blackbirdEventViewerContent);
            }
        } catch (LoginException e) {
            log.error("Cannot access content reader, {}", e.getMessage());
            throw e;
        } catch (RepositoryException e) {
            log.error("Failed to get resource SearchResult, {}", e.getMessage());
            throw new BlackbirdInternalErrorException(e.getMessage());
        }

        return BlackbirdPageEventSearchResult.builder()
                .results(results)
                .hasMore(hasMore)
                .totalMatches(totalMatches)
                .content(content).build();
    }

    private void setDaterangePredicate(DaterangePredicateParams params) {
        params.getQueryMap().put(params.getPrefix() + "_daterange.property", params.getProperty());
        params.getQueryMap().put(params.getPrefix() + "_daterange.lowerBound", params.getLowerBoundDate());
        params.getQueryMap().put(params.getPrefix() + "_daterange.upperBound", params.getUpperBoundDate());
    }

    private void addTagPredicates(Map<String, String> queryMap, Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        int predicateIndex = 1;
        for (String tag : tags) {
            queryMap.put(predicateIndex + "_property", "jcr:content/cq:tags");
            queryMap.put(predicateIndex + "_property.value", tag);
            predicateIndex++;
        }
        queryMap.put("allTags", "true");
    }

    @Value
    @Builder
    private static class DaterangePredicateParams {
        Map<String, String> queryMap;
        String prefix;
        String property;
        String lowerBoundDate;
        String upperBoundDate;
    }
}
