package io.blackbird.aemconnector.core.services.impl.detectors;

import com.day.cq.wcm.api.Page;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeDetector;
import io.blackbird.aemconnector.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AemContextExtension.class)
class PageDetectorTest {

    private final AemContext context = AppAemContext.newAemContext();
    private ContentTypeDetector detector;

    @BeforeEach
    void setUp() {
        detector = new PageDetector();
    }

    @Test
    void testWhenResourceIsNull() {
        assertFalse(detector.detects(null));
    }

    @Test
    void testWhenResourceIsNotPage() {
        final String path = "/content/en/foo";
        context.create().resource(path);

        Resource resource = context.resourceResolver().getResource(path);

        assertNotNull(resource);

        boolean detected = detector.detects(resource);

        assertFalse(detected);
    }

    @Test
    void testWhenResourceIsExperienceFragment() {
        final String path = "/content/experience-fragments/header";
        context.create().page(path);

        Resource resource = context.resourceResolver().getResource(path);

        assertNotNull(resource);
        assertNotNull(resource.adaptTo(Page.class));

        boolean detected = detector.detects(resource);

        assertFalse(detected);
    }

    @Test
    void testWhenResourceIsPage() {
        final String path = "/content/en/foo";
        context.create().page(path);

        Resource resource = context.resourceResolver().getResource(path);
        assertNotNull(resource);
        assertNotNull(resource.adaptTo(Page.class));

        boolean detected = detector.detects(resource);

        assertTrue(detected);
    }

    @Test
    void testGetContentType() {
        assertEquals(ContentType.PAGE, detector.getContentType());
    }
}