package io.blackbird.aemconnector.core.services.impl;

import com.adobe.cq.xf.ExperienceFragmentVariation;
import com.day.cq.wcm.api.Page;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.impl.detectors.ExperienceFragmentDetector;
import io.blackbird.aemconnector.core.services.impl.detectors.PageDetector;
import io.blackbird.aemconnector.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.LoginException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class ContentTypeServiceImplTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;
    @Mock
    private ExperienceFragmentVariation variation;

    private ContentTypeServiceImpl contentTypeService;

    @BeforeEach
    void setUp() throws LoginException {
        context.registerAdapter(Page.class, ExperienceFragmentVariation.class,
                (Function<Page, ExperienceFragmentVariation>) page -> variation);

        context.registerService(BlackbirdServiceUserResolverProvider.class, serviceUserResolverProvider);
        when(serviceUserResolverProvider.getContentStructureReaderResolver()).thenReturn(context.resourceResolver());

        contentTypeService = context.registerInjectActivateService(new ContentTypeServiceImpl());


        contentTypeService.bindDetector(new PageDetector());
        contentTypeService.bindDetector(new ExperienceFragmentDetector());

    }

    @Test
    void detectsPage() throws BlackbirdServiceException {
        final String path = "/content/en/page";
        context.create().page(path);

        final ContentType contentType = contentTypeService.resolveContentType(path);

        assertEquals(ContentType.PAGE, contentType);
    }

    @Test
    void detectsExperienceFragment() throws BlackbirdServiceException {
        final String path = "/content/experience-fragments/xf-page";
        context.create().page(path);

        ContentType contentType = contentTypeService.resolveContentType(path);

        assertEquals(ContentType.EXPERIENCE_FRAGMENT, contentType);
    }
}