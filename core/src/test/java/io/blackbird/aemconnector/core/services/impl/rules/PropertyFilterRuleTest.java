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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PropertyFilterRuleTest {

    @Test
    void shouldReturnTrueWhenPropertyMatchesCriteria() throws BlackbirdInternalErrorException {
        // GIVEN
        String pathContains = "/content/path";
        String propertyName = "testProperty";

        Property property = mock(Property.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn("/content/path/to/property");
            repositoryUtils.when(() -> RepositoryUtils.getName(property)).thenReturn(propertyName);

            PropertyFilterRule rule = PropertyFilterRule.builder()
                    .pathContains(pathContains)
                    .propertyName(propertyName)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertTrue(result);
        }
    }

    @Test
    void shouldReturnFalseWhenPathDoesNotContainCriteria() throws BlackbirdInternalErrorException {
        // GIVEN
        String pathContains = "/different/path";
        String propertyName = "testProperty";

        Property property = mock(Property.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn("/content/path/to/property");
            repositoryUtils.when(() -> RepositoryUtils.getName(property)).thenReturn(propertyName);

            PropertyFilterRule rule = PropertyFilterRule.builder()
                    .pathContains(pathContains)
                    .propertyName(propertyName)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnFalseWhenPropertyNameDoesNotMatch() throws BlackbirdInternalErrorException {
        // GIVEN
        String pathContains = "/content/path";
        String propertyName = "testProperty";

        Property property = mock(Property.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn("/content/path/to/property");
            repositoryUtils.when(() -> RepositoryUtils.getName(property)).thenReturn("differentProperty");

            PropertyFilterRule rule = PropertyFilterRule.builder()
                    .pathContains(pathContains)
                    .propertyName(propertyName)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldAlwaysReturnFalseForIsTranslatable() {
        // GIVEN
        String pathContains = "/content/path";
        String propertyName = "testProperty";

        Property property = mock(Property.class);

        PropertyFilterRule rule = PropertyFilterRule.builder()
                .pathContains(pathContains)
                .propertyName(propertyName)
                .build();

        // WHEN
        boolean result = rule.isTranslatable(property);

        // THEN
        assertFalse(result);
    }

    @Test
    void shouldPropagateExceptionWhenGetPathThrowsException() {
        // GIVEN
        String pathContains = "/content/path";
        String propertyName = "testProperty";

        Property property = mock(Property.class);
        BlackbirdInternalErrorException expectedException = new BlackbirdInternalErrorException("Test exception");

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenThrow(expectedException);

            PropertyFilterRule rule = PropertyFilterRule.builder()
                    .pathContains(pathContains)
                    .propertyName(propertyName)
                    .build();

            // WHEN & THEN
            assertThrows(BlackbirdInternalErrorException.class, () -> rule.appliesTo(property));
        }
    }

    @Test
    void shouldPropagateExceptionWhenGetNameThrowsException() {
        // GIVEN
        String pathContains = "/content/path";
        String propertyName = "testProperty";

        Property property = mock(Property.class);
        BlackbirdInternalErrorException expectedException = new BlackbirdInternalErrorException("Test exception");

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn("/content/path/to/property");
            repositoryUtils.when(() -> RepositoryUtils.getName(property)).thenThrow(expectedException);

            PropertyFilterRule rule = PropertyFilterRule.builder()
                    .pathContains(pathContains)
                    .propertyName(propertyName)
                    .build();

            // WHEN & THEN
            assertThrows(BlackbirdInternalErrorException.class, () -> rule.appliesTo(property));
        }
    }

}
