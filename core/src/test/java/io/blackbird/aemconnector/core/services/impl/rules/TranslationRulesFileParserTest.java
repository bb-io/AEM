package io.blackbird.aemconnector.core.services.impl.rules;

import io.blackbird.aemconnector.core.dto.TranslationRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith({MockitoExtension.class})
class TranslationRulesFileParserTest {

    private TranslationRulesFileParser target;

    @BeforeEach
    void setUp() {
        target = new TranslationRulesFileParser();
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInputStreamIsNull() {
        assertThrows(IllegalArgumentException.class, () -> target.parse(null),
                "Should throw IllegalArgumentException for null input stream");
    }

    @Test
    void shouldReturnEmptyRulesWhenIOException() throws IOException {
        // GIVEN
        InputStream inputStream = mock(InputStream.class);
        doThrow(new IOException("Simulated IOException for testing")).when(inputStream).read();

        // WHEN
        TranslationRules translationRules = target.parse(inputStream);

        // THEN
        assertNotNull(translationRules);
        assertTrue(translationRules.getContextRules().isEmpty());
    }

    @Test
    void shouldReturnEmptyRulesWhenXmlIsInvalid() {
        // GIVEN
        String invalidXml = "<invalid>xml</invalid>";
        InputStream inputStream = new ByteArrayInputStream(invalidXml.getBytes(StandardCharsets.UTF_8));

        // WHEN
        TranslationRules translationRules = target.parse(inputStream);

        // THEN
        assertNotNull(translationRules);
        assertTrue(translationRules.getContextRules().isEmpty());
    }

    @Test
    void shouldReturnEmptyRulesWhenXmlIsEmpty() {
        // GIVEN
        String emptyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><nodelist></nodelist>";
        InputStream inputStream = new ByteArrayInputStream(emptyXml.getBytes(StandardCharsets.UTF_8));

        // WHEN
        TranslationRules translationRules = target.parse(inputStream);

        // THEN
        assertNotNull(translationRules);
        assertTrue(translationRules.getContextRules().isEmpty());
    }

    @Test
    void shouldReturnParsedRules() throws Exception {
        // GIVEN
        InputStream inputStream = getClass().getResourceAsStream("/translation_rules-parser-test.xml");

        // WHEN
        TranslationRules translationRules = target.parse(inputStream);

        // THEN
        assertNotNull(translationRules);
        assertFalse(translationRules.getContextRules().isEmpty());
        assertEquals(2, translationRules.getContextRules().size());
        assertEquals("/content/test", translationRules.getContextRules().get(0).getContextPath());
        assertEquals("/content", translationRules.getContextRules().get(1).getContextPath());
        assertFalse(translationRules.getAssetReferenceRules().isEmpty());
        assertEquals(1, translationRules.getAssetReferenceRules().size());
        AssetReferenceRule assetReferenceRule = translationRules.getAssetReferenceRules().get(0);
        assertEquals("AssetReferenceRule(assetReferenceAttribute=fragmentPath, resourceType=cq/experience-fragments/editor/components/experiencefragment, checkInChildNodes=false, createLangCopy=true)",
                assetReferenceRule.toString());
    }

}
