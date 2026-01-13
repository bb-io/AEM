package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.ContentType;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateTagsServiceImplTest {

    @Mock
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private Resource resource;

    @Mock
    private Resource childResource;

    @Mock
    private ModifiableValueMap modifiableValueMap;

    @InjectMocks
    private UpdateTagsServiceImpl updateTagsService;

    @BeforeEach
    void setUp() throws Exception {
        when(serviceUserResolverProvider.getTranslationWriterResolver()).thenReturn(resolver);
    }

    @Test
    void shouldUpdateTagsForPageResourceIfTagsToAndAndTagsToRemoveExist() throws Exception {
        Set<String> tagsToAdd = new HashSet<>(Arrays.asList("tag3"));
        Set<String> tagsToRemove = new HashSet<>(Arrays.asList("tag1"));
        when(resolver.getResource("/content/page")).thenReturn(resource);
        when(resolver.getResource("/content/page/jcr:content")).thenReturn(childResource);
        when(childResource.adaptTo(ModifiableValueMap.class)).thenReturn(modifiableValueMap);
        when(modifiableValueMap.get("cq:tags", String[].class)).thenReturn(new String[]{"tag1", "tag2"});

        Resource result = updateTagsService.updateTags("/content/page", tagsToAdd, tagsToRemove, ContentType.PAGE);

        assertEquals(resource, result);
        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(modifiableValueMap).put(eq("cq:tags"), captor.capture());
        Set<String> updatedTags = new HashSet<>(Arrays.asList(captor.getValue()));
        assertEquals(new HashSet<>(Arrays.asList("tag2", "tag3")), updatedTags);
        verify(resolver).commit();
    }

    @Test
    void shouldUpdateTagsForAssetResourceIfOnlyTagsToAddExist() throws Exception {
        Set<String> tagsToAdd = new HashSet<>(Arrays.asList("tag"));
        when(resolver.getResource("/content/dam/asset")).thenReturn(resource);
        when(resolver.getResource("/content/dam/asset/jcr:content/metadata")).thenReturn(childResource);
        when(childResource.adaptTo(ModifiableValueMap.class)).thenReturn(modifiableValueMap);
        when(modifiableValueMap.get("cq:tags", String[].class)).thenReturn(null);

        updateTagsService.updateTags("/content/dam/asset", tagsToAdd, null, ContentType.ASSET);

        verify(modifiableValueMap).put(eq("cq:tags"), aryEq(new String[]{"tag"}));
        verify(resolver).commit();
    }

    @Test
    void shouldThrowExceptionIfResourceDoesNotExist() throws Exception {
        when(resolver.getResource("/content/page")).thenReturn(null);

        BlackbirdServiceException exception = assertThrows(BlackbirdServiceException.class, () -> updateTagsService
                .updateTags("/content/page", new HashSet<>(Arrays.asList("tag")), null, ContentType.PAGE)
        );

        assertEquals("Resource does not exist '/content/page'", exception.getMessage());
        verify(resolver, never()).commit();
    }

    @Test
    void shouldThrowExceptionIfChildResourceDoesNotExist() throws Exception {
        when(resolver.getResource("/content/page")).thenReturn(resource);
        when(resolver.getResource("/content/page/jcr:content")).thenReturn(null);

        BlackbirdServiceException exception = assertThrows(BlackbirdServiceException.class, () -> updateTagsService
                .updateTags("/content/page", new HashSet<>(Arrays.asList("tag")), null, ContentType.PAGE)
        );

        assertEquals("Child resource does not exist '/content/page'", exception.getMessage());
        verify(resolver, never()).commit();
    }

    @Test
    void shouldThrowExceptionIfResourceHasUnknownContentType() throws Exception {
        when(resolver.getResource("/content/unknown")).thenReturn(resource);

        BlackbirdServiceException exception = assertThrows(BlackbirdServiceException.class, () -> updateTagsService
                .updateTags("/content/unknown", new HashSet<>(Arrays.asList("tag")), null, ContentType.UNKNOWN)
        );

        assertEquals("Child resource does not exist '/content/unknown'", exception.getMessage());
        verify(resolver, never()).commit();
    }

    @Test
    void shouldThrowExceptionIfModifiableValueMapIsNull() throws Exception {
        when(resolver.getResource("/content/page")).thenReturn(resource);
        when(resolver.getResource("/content/page/jcr:content")).thenReturn(childResource);
        when(childResource.getPath()).thenReturn("/content/page/jcr:content");
        when(childResource.adaptTo(ModifiableValueMap.class)).thenReturn(null);

        BlackbirdServiceException exception = assertThrows(BlackbirdServiceException.class, () -> updateTagsService
                .updateTags("/content/page", new HashSet<>(Arrays.asList("tag")), null, ContentType.PAGE)
        );

        assertEquals("Can not adapt resource to modifiableValueMap '/content/page/jcr:content'", exception.getMessage());
        verify(resolver, never()).commit();
    }

}
