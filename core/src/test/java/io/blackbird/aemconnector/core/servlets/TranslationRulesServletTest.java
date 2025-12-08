package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.dto.TranslationRules;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationRulesServletTest {

    @Mock
    private TranslationRulesService translationRulesService;

    @InjectMocks
    private TranslationRulesServlet servlet;

    private TranslationRules translationRules;

    @BeforeEach
    void setUp() {
        translationRules = TranslationRules.EMPTY;
    }

    @Test
    void buildResponsePayloadShouldReturnTranslationRules() throws BlackbirdHttpErrorException, BlackbirdInternalErrorException {
        when(translationRulesService.getTranslationsRules()).thenReturn(translationRules);

        Serializable serializable = servlet.buildResponsePayload(null, null);

        assertNotNull(serializable);
        assertEquals(translationRules, serializable);
        verify(translationRulesService).getTranslationsRules();
    }

    @Test
    void shouldThrowExceptionWhenGettingTranslationRules() throws BlackbirdInternalErrorException {
        String expectedErrorMsg = "Translation Rules Error";
        when(translationRulesService.getTranslationsRules()).thenThrow(new BlackbirdInternalErrorException(expectedErrorMsg));

        BlackbirdHttpErrorException ex = assertThrows(
                BlackbirdHttpErrorException.class,
                () -> servlet.buildResponsePayload(null, null));

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getStatus());
        assertEquals(expectedErrorMsg, ex.getMessage());
    }

    @Test
    void shouldBuildXmlResponsePayload() throws BlackbirdHttpErrorException {
        InputStream mockStream = new ByteArrayInputStream("<xml/>".getBytes());
        when(translationRulesService.getTranslationRulesFileInputStream())
                .thenReturn(Optional.of(mockStream));

        InputStream result = servlet.buildXmlResponsePayload(null, null);

        assertNotNull(result);
        assertSame(mockStream, result);
        verify(translationRulesService).getTranslationRulesFileInputStream();
    }

    @Test
    void testBuildXmlResponsePayload_WhenEmptyOptional_ThrowsNotFound() {
        when(translationRulesService.getTranslationRulesFileInputStream())
                .thenReturn(Optional.empty());

        BlackbirdHttpErrorException ex = assertThrows(
                BlackbirdHttpErrorException.class,
                () -> servlet.buildXmlResponsePayload(null, null)
        );

        assertEquals(404, ex.getStatus());
        assertTrue(ex.getMessage().contains("Translation Rules file not found"));
    }
}