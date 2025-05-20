package io.blackbird.aemconnector.core.services.impl.rules;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.utils.RepositoryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Property;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GeneralRuleTest {

    @Test
    void shouldReturnTrueWhenPropertyNameMatchesRuleName() throws BlackbirdInternalErrorException {
        // GIVEN
        String propertyName = "jcr:title";
        boolean translate = true;
        boolean inherit = false;
        boolean updateDestinationLanguage = false;

        Property property = mock(Property.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getName(property)).thenReturn(propertyName);

            GeneralRule rule = GeneralRule.builder()
                    .name(propertyName)
                    .translate(translate)
                    .inherit(inherit)
                    .updateDestinationLanguage(updateDestinationLanguage)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertTrue(result);
        }
    }

    @Test
    void shouldReturnFalseWhenPropertyNameDoesNotMatchRuleName() throws BlackbirdInternalErrorException {
        // GIVEN
        String ruleName = "jcr:title";
        String propertyName = "jcr:description";
        boolean translate = true;
        boolean inherit = false;
        boolean updateDestinationLanguage = false;

        Property property = mock(Property.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getName(property)).thenReturn(propertyName);

            GeneralRule rule = GeneralRule.builder()
                    .name(ruleName)
                    .translate(translate)
                    .inherit(inherit)
                    .updateDestinationLanguage(updateDestinationLanguage)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnTranslateFlagWhenIsTranslatableIsCalled() {
        // GIVEN
        String propertyName = "jcr:title";
        boolean translate = true;
        boolean inherit = false;
        boolean updateDestinationLanguage = false;

        Property property = mock(Property.class);

        GeneralRule rule = GeneralRule.builder()
                .name(propertyName)
                .translate(translate)
                .inherit(inherit)
                .updateDestinationLanguage(updateDestinationLanguage)
                .build();

        // WHEN
        boolean result = rule.isTranslatable(property);

        // THEN
        assertTrue(result);
    }

}
