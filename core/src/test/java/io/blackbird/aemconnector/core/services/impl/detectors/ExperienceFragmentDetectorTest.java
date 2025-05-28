package io.blackbird.aemconnector.core.services.impl.detectors;

import com.adobe.cq.xf.ExperienceFragmentVariation;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class ExperienceFragmentDetectorTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private ExperienceFragmentVariation variation;
    private ContentTypeDetector detector;

    @BeforeEach
    void setUp() {
        detector = new ExperienceFragmentDetector();
    }

    @Test
    void testDetectsExperienceFragmentTrue() {
        final String path = "/content/experience-fragments/wknd/language-masters/en/site/header/master";
        final String template = "/conf/wknd/settings/wcm/templates/experience-fragment-web-variation-template";
        context.create().page(path, template);

        context.registerAdapter(Page.class, ExperienceFragmentVariation.class,
                (Function<Page, ExperienceFragmentVariation>) page -> variation);

        Resource resource = context.resourceResolver().getResource(path);

        boolean detected = detector.detects(resource);

        assertTrue(detected);
    }

    @Test
    void testWhenPageNotXFvariant() {
        final String path = "/content/en/foo";
        context.create().page(path);

        Resource resource = context.resourceResolver().getResource("/content/en/foo");
        assertNotNull(resource);

        boolean detected = detector.detects(resource);

        assertFalse(detected);
    }

    @Test
    void testWhenResourceIsNull() {
        assertFalse(detector.detects(null));
    }

    @Test
    void testGetContentType() {
        assertEquals(ContentType.EXPERIENCE_FRAGMENT, detector.getContentType());
    }
}