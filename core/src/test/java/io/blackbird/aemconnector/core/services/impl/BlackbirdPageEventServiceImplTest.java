package io.blackbird.aemconnector.core.services.impl;

import com.day.cq.search.Predicate;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.google.common.collect.Sets;
import io.blackbird.aemconnector.core.dto.BlackbirdPageEventSearchResult;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.models.BlackbirdEventViewerPage;
import io.blackbird.aemconnector.core.objects.PageEventSearchParams;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class BlackbirdPageEventServiceImplTest {

    @Mock
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;
    @Mock
    private ResourceResolver resourceResolver;
    @Mock
    private Session session;
    @Mock
    private QueryBuilder queryBuilder;
    @Mock
    private Query query;
    @Mock
    private SearchResult searchResult;
    @Mock
    private Hit hit;
    @Mock
    private Resource resource;
    @Mock
    private BlackbirdEventViewerPage blackbirdEventViewerPage;


    @InjectMocks
    private BlackbirdPageEventServiceImpl target;

    @Test
    void testSearchPageEventsCreatedOnly() throws BlackbirdInternalErrorException, LoginException, RepositoryException {

        setUpMocksForSuccessfulCases();

        PageEventSearchParams params = PageEventSearchParams.builder()
                .rootPath("/content")
                .startDate("2025-03-01")
                .endDate("2025-03-05")
                .limit(8)
                .offset(0)
                .events(Sets.newHashSet(BlackbirdPageEventServiceImpl.CREATED))
                .build();
        BlackbirdPageEventSearchResult result = target.searchPageEvents(params);

        assertNotNull(result);
        assertEquals(1, result.getResults());
        assertFalse(result.isHasMore());
        assertEquals(1L, result.getTotalMatches());
        assertEquals(1, result.getPages().size());

        ArgumentCaptor<PredicateGroup> predicateGroupCaptor = ArgumentCaptor.forClass(PredicateGroup.class);
        verify(queryBuilder).createQuery(predicateGroupCaptor.capture(), eq(session));
        PredicateGroup capturedPredicateGroup = predicateGroupCaptor.getValue();
        Predicate daterangePredicate = capturedPredicateGroup.getByPath("group.1_daterange.property");

        assertNotNull(daterangePredicate);
        assertEquals("jcr:created", daterangePredicate.get("property"));
    }

    @Test
    void testSearchPageEventsModifiedOnly() throws BlackbirdInternalErrorException, LoginException, RepositoryException {

        setUpMocksForSuccessfulCases();

        PageEventSearchParams params = PageEventSearchParams.builder()
                .rootPath("/content")
                .startDate("2025-03-01")
                .endDate("2025-03-05")
                .limit(8)
                .offset(0)
                .events(Sets.newHashSet(BlackbirdPageEventServiceImpl.MODIFIED))
                .build();
        BlackbirdPageEventSearchResult result = target.searchPageEvents(params);

        assertNotNull(result);
        assertEquals(1, result.getResults());
        assertFalse(result.isHasMore());
        assertEquals(1L, result.getTotalMatches());
        assertEquals(1, result.getPages().size());

        ArgumentCaptor<PredicateGroup> predicateGroupCaptor = ArgumentCaptor.forClass(PredicateGroup.class);
        verify(queryBuilder).createQuery(predicateGroupCaptor.capture(), eq(session));
        PredicateGroup capturedPredicateGroup = predicateGroupCaptor.getValue();
        Predicate daterangePredicate = capturedPredicateGroup.getByPath("group.1_daterange.property");

        assertNotNull(daterangePredicate);
        assertEquals("jcr:content/cq:lastModified", daterangePredicate.get("property"));
    }

    @Test
    void testSearchPageEventsCreatedAndModified() throws BlackbirdInternalErrorException, LoginException, RepositoryException {
        setUpMocksForSuccessfulCases();

        PageEventSearchParams params = PageEventSearchParams.builder()
                .rootPath("/content")
                .startDate("2025-03-01")
                .endDate("2025-03-05")
                .limit(8)
                .offset(0)
                .events(Sets.newHashSet(BlackbirdPageEventServiceImpl.CREATED, BlackbirdPageEventServiceImpl.MODIFIED))
                .build();
        BlackbirdPageEventSearchResult result = target.searchPageEvents(params);

        assertNotNull(result);
        assertEquals(1, result.getResults());
        assertFalse(result.isHasMore());
        assertEquals(1L, result.getTotalMatches());
        assertEquals(1, result.getPages().size());

        ArgumentCaptor<PredicateGroup> predicateGroupCaptor = ArgumentCaptor.forClass(PredicateGroup.class);
        verify(queryBuilder).createQuery(predicateGroupCaptor.capture(), eq(session));
        PredicateGroup capturedPredicateGroup = predicateGroupCaptor.getValue();

        Predicate daterangePredicate1 = capturedPredicateGroup.getByPath("group.1_daterange.property");
        assertNotNull(daterangePredicate1);
        assertEquals("jcr:created", daterangePredicate1.get("property"));

        Predicate daterangePredicate2 = capturedPredicateGroup.getByPath("group.2_daterange.property");
        assertNotNull(daterangePredicate2);
        assertEquals("jcr:content/cq:lastModified", daterangePredicate2.get("property"));
    }

    @Test
    void testSearchPageEventsWithTags() throws BlackbirdInternalErrorException, LoginException, RepositoryException {

        setUpMocksForSuccessfulCases();

        PageEventSearchParams params = PageEventSearchParams.builder()
                .rootPath("/content")
                .startDate("2025-03-01")
                .endDate("2025-03-05")
                .limit(8)
                .offset(0)
                .events(Sets.newHashSet(BlackbirdPageEventServiceImpl.CREATED))
                .tags(Sets.newHashSet("test-tag"))
                .build();
        BlackbirdPageEventSearchResult result = target.searchPageEvents(params);

        assertNotNull(result);
        assertEquals(1, result.getResults());
        assertFalse(result.isHasMore());
        assertEquals(1L, result.getTotalMatches());
        assertEquals(1, result.getPages().size());

        ArgumentCaptor<PredicateGroup> predicateGroupCaptor = ArgumentCaptor.forClass(PredicateGroup.class);
        verify(queryBuilder).createQuery(predicateGroupCaptor.capture(), eq(session));
        PredicateGroup capturedPredicateGroup = predicateGroupCaptor.getValue();
        Predicate tagsPredicate = capturedPredicateGroup.getByName("1_property");

        assertNotNull(tagsPredicate);
        assertEquals("test-tag", tagsPredicate.get("value"));
        assertEquals("jcr:content/cq:tags", tagsPredicate.get("property"));
    }

    @Test
    void testSearchPageEventsLoginException() throws Exception {
        PageEventSearchParams params = PageEventSearchParams.builder()
                .rootPath("/content")
                .startDate("2025-03-01")
                .endDate("2025-03-05")
                .limit(8)
                .offset(0)
                .events(Sets.newHashSet(BlackbirdPageEventServiceImpl.CREATED, BlackbirdPageEventServiceImpl.MODIFIED))
                .build();

        when(serviceUserResolverProvider.getPageContentReaderResolver())
                .thenThrow(new LoginException("Login error"));

        LoginException exception =
                assertThrows(LoginException.class, () -> target.searchPageEvents(params));
        assertEquals("Login error", exception.getMessage());
    }

    @Test
    void testSearchPageEventsRepositoryException() throws Exception {
        PageEventSearchParams params = PageEventSearchParams.builder()
                .rootPath("/content")
                .startDate("2025-03-01")
                .endDate("2025-03-05")
                .limit(8)
                .offset(0)
                .events(Sets.newHashSet(BlackbirdPageEventServiceImpl.CREATED, BlackbirdPageEventServiceImpl.MODIFIED))
                .build();

        when(serviceUserResolverProvider.getPageContentReaderResolver()).thenReturn(resourceResolver);
        when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
        when(queryBuilder.createQuery(any(PredicateGroup.class), eq(session))).thenReturn(query);

        when(query.getResult()).thenReturn(searchResult);
        when(searchResult.getTotalMatches()).thenReturn(1L);
        when(searchResult.hasMore()).thenReturn(false);

        List<Hit> hits = Collections.singletonList(hit);
        when(searchResult.getHits()).thenReturn(hits);
        when(hit.getResource()).thenThrow(new RepositoryException("Repository error"));

        BlackbirdInternalErrorException exception =
                assertThrows(BlackbirdInternalErrorException.class, () -> target.searchPageEvents(params));
        assertEquals("Repository error", exception.getMessage());
    }


    private void setUpMocksForSuccessfulCases() throws LoginException, RepositoryException {
        when(serviceUserResolverProvider.getPageContentReaderResolver()).thenReturn(resourceResolver);
        when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
        when(queryBuilder.createQuery(any(PredicateGroup.class), eq(session))).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
        when(searchResult.getTotalMatches()).thenReturn(1L);
        when(searchResult.hasMore()).thenReturn(false);

        List<Hit> hits = Collections.singletonList(hit);
        when(searchResult.getHits()).thenReturn(hits);
        when(hit.getResource()).thenReturn(resource);
        when(resource.adaptTo(BlackbirdEventViewerPage.class)).thenReturn(blackbirdEventViewerPage);
    }
}