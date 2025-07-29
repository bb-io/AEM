package io.blackbird.aemconnector.core.services.impl.detectors;

import io.blackbird.aemconnector.core.services.ContentType;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DitaDetectorTest {

    private DitaDetector detector;

    @Mock
    private Resource resource;

    @BeforeEach
    void setUp() {
        detector = new DitaDetector();
    }

    @Nested
    @DisplayName("detects(resource) -> true for valid DITA extensions")
    class ValidExtensions {
        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "/content/path/file.xml",
                "/content/path/topic.dita",
                "/foo/bar/map.ditamap",
                "/baz/qux/filters.ditaval",
                "/some.dir/other.dir/my.topic.dita"
        })
        void shouldReturnTrue(String path) {
            when(resource.getPath()).thenReturn(path);
            assertTrue(detector.detects(resource));
        }
    }

    @Nested
    @DisplayName("detects(resource) -> false for non‑DITA or edge cases")
    class InvalidExtensions {
        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "/content/path/file.txt",
                "/content/path/image.jpeg",
                "/foo/bar/.xmlhidden",
                "/baz/qux/file.",
                "/noextension",
        })
        void shouldReturnFalse(String path) {
            when(resource.getPath()).thenReturn(path);
            assertFalse(detector.detects(resource));
        }
    }
    
    @Test
    void testGetContentType() {
        assertEquals(ContentType.DITA, detector.getContentType());
    }

    @Test
    void testGetRank() {
        assertEquals(1, detector.getRank());
    }
}