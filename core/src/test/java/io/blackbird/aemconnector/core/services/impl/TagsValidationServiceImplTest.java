package io.blackbird.aemconnector.core.services.impl;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TagsValidationServiceImplTest {

    @Mock
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private TagManager tagManager;

    @Mock
    private Tag tag;

    @InjectMocks
    private TagsValidationServiceImpl tagsValidationService;

    @BeforeEach
    void setUp() throws Exception {
        when(serviceUserResolverProvider.getTranslationWriterResolver()).thenReturn(resolver);
    }

    @Test
    void shouldNotThrowExceptionIfAllTagsExist() {
        Set<String> tags = new HashSet<>(Arrays.asList("tag:test1", "tag:test2"));
        when(resolver.adaptTo(TagManager.class)).thenReturn(tagManager);
        when(tagManager.resolve(anyString())).thenReturn(tag);

        assertDoesNotThrow(() -> tagsValidationService.validateTags(tags));

        verify(tagManager, times(2)).resolve(anyString());
    }

    @Test
    void shouldNotThrowExceptionIfTagExists() {
        Set<String> tags = new HashSet<>(Arrays.asList("tag:test"));
        when(resolver.adaptTo(TagManager.class)).thenReturn(tagManager);
        when(tagManager.resolve("tag:test")).thenReturn(tag);

        assertDoesNotThrow(() -> tagsValidationService.validateTags(tags));

        verify(tagManager).resolve("tag:test");
    }

    @Test
    void shouldThrowExceptionIfTagDoesNotExist() {
        Set<String> tags = new HashSet<>(Arrays.asList("tag:invalid"));
        when(resolver.adaptTo(TagManager.class)).thenReturn(tagManager);
        when(tagManager.resolve("tag:invalid")).thenReturn(null);

        BlackbirdServiceException exception = assertThrows(
                BlackbirdServiceException.class, () -> tagsValidationService.validateTags(tags)
        );

        assertEquals("Tags validation fails. Tag does not exist: 'tag:invalid'", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfTagManagerIsNull() {
        when(resolver.adaptTo(TagManager.class)).thenReturn(null);

        BlackbirdServiceException exception = assertThrows(
                BlackbirdServiceException.class,
                () -> tagsValidationService.validateTags(new HashSet<>(Collections.singletonList("tag:test")))
        );

        assertEquals("Can not validate tags. TagManager is null", exception.getMessage());
    }

    @Test
    void shouldNotThrowExceptionIfTagsAreEmpty() {
        when(resolver.adaptTo(TagManager.class)).thenReturn(tagManager);
        Set<String> tags = Collections.emptySet();

        assertDoesNotThrow(() -> tagsValidationService.validateTags(tags));

        verifyNoInteractions(tagManager);
    }

}
