package io.blackbird.aemconnector.core.models;

import io.blackbird.aemconnector.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junitx.framework.Assert.assertEquals;

@ExtendWith(AemContextExtension.class)
class BlackbirdEventViewerContentTest {

    private final AemContext context = AppAemContext.newAemContext();

    private BlackbirdEventViewerContent model;

    @BeforeEach
    void setUp() {
        context.load().json("/content/base-content.json", "/content");
    }

    @Test
    void shouldReturnDifferentDatesWhenCreatedAndModifiedDatesAreDifferent() {
        context.currentResource("/content/experience-fragments/bb-aem-connector/us/en/site/header");

        model = context.currentResource().adaptTo(BlackbirdEventViewerContent.class);

        assertNotNull(model);
        assertEquals("Header", model.getTitle());
        assertEquals("/content/experience-fragments/bb-aem-connector/us/en/site/header", model.getPath());
        assertEquals(Instant.parse("2025-03-24T17:18:29Z"), model.getCreated());
        assertEquals(Instant.parse("2025-03-24T17:25:41Z"), model.getModified());
    }

    @Test
    void shouldReturnModifiedDateNullWhenCreatedAndModifiedDatesAreSame() {
        context.currentResource("/content/experience-fragments/bb-aem-connector/us/en/site/header/master/jcr:content/root/logo/logo.svg");

        model = context.currentResource().adaptTo(BlackbirdEventViewerContent.class);

        assertNotNull(model);
        assertNull(model.getTitle());
        assertEquals("/content/experience-fragments/bb-aem-connector/us/en/site/header/master/jcr:content/root/logo/logo.svg", model.getPath());
        assertEquals(Instant.parse("2025-03-24T17:18:29Z"), model.getCreated());
        assertNull(model.getModified());
    }

    @Test
    void shouldReturnJcrTitleWhenPageTitleIsEmpty() {
        context.currentResource("/content/bb-aem-connector/us/en/search");

        model = context.currentResource().adaptTo(BlackbirdEventViewerContent.class);

        assertNotNull(model);
        assertEquals("Search Results", model.getTitle());
        assertEquals("/content/bb-aem-connector/us/en/search", model.getPath());
        assertEquals(Instant.parse("2025-03-24T12:10:30Z"), model.getCreated());
        assertNull(model.getModified());
    }

    @Test
    void shouldReturnPageTitleWhenPageTitleIsPresent() {
        context.currentResource("/content/bb-aem-connector/us/en/reset-password");

        model = context.currentResource().adaptTo(BlackbirdEventViewerContent.class);

        assertNotNull(model);
        assertEquals("Reset Password", model.getTitle());
        assertEquals("/content/bb-aem-connector/us/en/reset-password", model.getPath());
        assertNull(model.getCreated());
        assertNull(model.getModified());
    }

    @Test
    void shouldReturnPageNameWhenPageTitleAndJcrTitleAreEmpty() {
        context.currentResource("/content/bb-aem-connector/us/en/pageExport");

        model = context.currentResource().adaptTo(BlackbirdEventViewerContent.class);

        assertNotNull(model);
        assertEquals("pageExport", model.getTitle());
        assertEquals("/content/bb-aem-connector/us/en/pageExport", model.getPath());
        assertNull(model.getCreated());
        assertNull(model.getModified());
    }

    @Test
    void shouldReturnJcrTitleWhenJcrTitleIsPresentForAsset() {
        context.currentResource("/content/content-fragments/bb-aem-connector/us/en/cf-test");

        model = context.currentResource().adaptTo(BlackbirdEventViewerContent.class);

        assertNotNull(model);
        assertEquals("Test", model.getTitle());
        assertEquals("/content/content-fragments/bb-aem-connector/us/en/cf-test", model.getPath());
        assertEquals(Instant.parse("2025-03-24T13:30:30Z"), model.getCreated());
        assertNull(model.getModified());
    }
}
