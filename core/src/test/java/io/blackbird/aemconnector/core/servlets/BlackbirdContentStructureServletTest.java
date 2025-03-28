package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.models.BlackbirdContentStructureModel;
import io.blackbird.aemconnector.core.services.BlackbirdContentStructureService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class BlackbirdContentStructureServletTest {
    private static final String SUFFIX = "/content/bb-aem-connector";
    private static final String SERVLET_RESOURCE_TYPE = "/services/bb-aem-connector/content";

    private BlackbirdContentStructureServlet fixture = new BlackbirdContentStructureServlet();

    @Mock
    private BlackbirdContentStructureService service;

    @BeforeEach
    void setup(AemContext context) {
        context.registerService(BlackbirdContentStructureService.class, service);

        context.registerInjectActivateService(fixture);
        context.create().resource(SERVLET_RESOURCE_TYPE);
        context.currentResource(SERVLET_RESOURCE_TYPE);
    }

    @Test
    void returnsRealModelWhenSuffixIsValid(AemContext context) {
        context.requestPathInfo().setSuffix(SUFFIX);
        when(service.getContentStructure(eq(SUFFIX))).thenReturn(any(BlackbirdContentStructureModel.class));

        fixture.getSerializableObject(context.request(), context.response());

        assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
        verify(service).getContentStructure(eq(SUFFIX));
    }

    @Test
    void nullResultWhenSuffixMissing(AemContext context) {
        context.requestPathInfo().setSuffix("");

        Serializable result = fixture.getSerializableObject(context.request(), context.response());

        assertNull(result);
    }

    @Test
    void nullResultWhenSuffixNotFound(AemContext context) {
        context.requestPathInfo().setSuffix("/content/does-not-exist");

        Serializable result = fixture.getSerializableObject(context.request(), context.response());

        assertNull(result);
    }
}