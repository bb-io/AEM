package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.constants.ServletConstants;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
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
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class BlackbirdContentStructureServletTest {
    private static final String CONTENT_PATH = "/content/bb-aem-connector";

    private BlackbirdContentStructureServlet fixture = new BlackbirdContentStructureServlet();

    @Mock
    private BlackbirdContentStructureService service;

    @BeforeEach
    void setup(AemContext context) {
        context.registerService(BlackbirdContentStructureService.class, service);

        context.registerInjectActivateService(fixture);
        context.create().resource("/content/services/bb-aem-connector/content");
        context.currentResource("/content/services/bb-aem-connector/content");
    }

    @Test
    void returnsRealModelWhenParamIsValid(AemContext context) throws BlackbirdHttpErrorException {
        context.request().setParameterMap(Collections.singletonMap(ServletConstants.CONTENT_PATH_PARAM, CONTENT_PATH));

        when(service.getContentStructure(eq(CONTENT_PATH))).thenReturn(new BlackbirdContentStructureModel());

        fixture.buildResponsePayload(context.request(), context.response());

        assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
        verify(service).getContentStructure(eq(CONTENT_PATH));
    }

    @Test
    void throwsBadRequestWhenPagePathParamMissing(AemContext context) {

        BlackbirdHttpErrorException ex = assertThrows(BlackbirdHttpErrorException.class, () -> fixture.buildResponsePayload(context.request(), context.response()));

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getStatus());
    }

    @Test
    void throwNotFoundWhenPathNotFound(AemContext context) throws BlackbirdHttpErrorException {
        context.request().setParameterMap(Collections.singletonMap(ServletConstants.CONTENT_PATH_PARAM, CONTENT_PATH));
        when(service.getContentStructure(eq(CONTENT_PATH))).thenReturn(null);


        BlackbirdHttpErrorException ex = assertThrows(BlackbirdHttpErrorException.class, () -> fixture.buildResponsePayload(context.request(), context.response()));
        assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getStatus());
    }
}