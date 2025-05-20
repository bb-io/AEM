package io.blackbird.aemconnector.core.services.impl.rules;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.utils.RepositoryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class TranslationNodeFilterRuleTest {

    private static final String PROPERTY_NAME = "testProperty";
    private static final String PROPERTY_VALUE = "testValue";

    @Test
    void shouldReturnTrueWhenNodeHasPropertyWithMatchingValue() throws BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.hasProperty(node, PROPERTY_NAME)).thenReturn(true);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(node, PROPERTY_NAME)).thenReturn(PROPERTY_VALUE);

            TranslationNodeFilterRule rule = TranslationNodeFilterRule.builder()
                    .propertyName(PROPERTY_NAME)
                    .propertyValue(PROPERTY_VALUE)
                    .isDeep(false)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(node);

            // THEN
            assertTrue(result);
        }
    }

    @Test
    void shouldReturnFalseWhenNodeDoesNotHaveProperty() throws BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.hasProperty(node, PROPERTY_NAME)).thenReturn(false);

            TranslationNodeFilterRule rule = TranslationNodeFilterRule.builder()
                    .propertyName(PROPERTY_NAME)
                    .propertyValue(PROPERTY_VALUE)
                    .isDeep(false)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(node);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnFalseWhenNodeHasPropertyWithDifferentValue() throws BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.hasProperty(node, PROPERTY_NAME)).thenReturn(true);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(node, PROPERTY_NAME)).thenReturn("differentValue");

            TranslationNodeFilterRule rule = TranslationNodeFilterRule.builder()
                    .propertyName(PROPERTY_NAME)
                    .propertyValue(PROPERTY_VALUE)
                    .isDeep(false)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(node);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnTrueWhenParentNodeHasPropertyWithMatchingValueAndIsDeepIsTrue() throws BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);
        Node parentNode = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.hasProperty(node, PROPERTY_NAME)).thenReturn(false);
            repositoryUtils.when(() -> RepositoryUtils.getParent(node)).thenReturn(parentNode);
            repositoryUtils.when(() -> RepositoryUtils.hasProperty(parentNode, PROPERTY_NAME)).thenReturn(true);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(parentNode, PROPERTY_NAME)).thenReturn(PROPERTY_VALUE);
            repositoryUtils.when(() -> RepositoryUtils.getPath(parentNode)).thenReturn("/content/path");

            TranslationNodeFilterRule rule = TranslationNodeFilterRule.builder()
                    .propertyName(PROPERTY_NAME)
                    .propertyValue(PROPERTY_VALUE)
                    .isDeep(true)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(node);

            // THEN
            assertTrue(result);
        }
    }

    @Test
    void shouldReturnFalseWhenNoNodeInHierarchyHasPropertyAndIsDeepIsTrue() throws BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);
        Node parentNode = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.hasProperty(node, PROPERTY_NAME)).thenReturn(false);
            repositoryUtils.when(() -> RepositoryUtils.getParent(node)).thenReturn(parentNode);
            repositoryUtils.when(() -> RepositoryUtils.hasProperty(parentNode, PROPERTY_NAME)).thenReturn(false);
            repositoryUtils.when(() -> RepositoryUtils.getParent(parentNode)).thenReturn(null);

            TranslationNodeFilterRule rule = TranslationNodeFilterRule.builder()
                    .propertyName(PROPERTY_NAME)
                    .propertyValue(PROPERTY_VALUE)
                    .isDeep(true)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(node);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnFalseWhenParentNodeHasPropertyWithDifferentValueAndIsDeepIsTrue() throws BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);
        Node parentNode = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.hasProperty(node, PROPERTY_NAME)).thenReturn(false);
            repositoryUtils.when(() -> RepositoryUtils.getParent(node)).thenReturn(parentNode);
            repositoryUtils.when(() -> RepositoryUtils.hasProperty(parentNode, PROPERTY_NAME)).thenReturn(true);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(parentNode, PROPERTY_NAME)).thenReturn("differentValue");
            repositoryUtils.when(() -> RepositoryUtils.getParent(parentNode)).thenReturn(null);

            TranslationNodeFilterRule rule = TranslationNodeFilterRule.builder()
                    .propertyName(PROPERTY_NAME)
                    .propertyValue(PROPERTY_VALUE)
                    .isDeep(true)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(node);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnNonTranslatableWhenIsDeepIsTrue() {
        // GIVEN
        TranslationNodeFilterRule rule = TranslationNodeFilterRule.builder()
                .propertyName(PROPERTY_NAME)
                .propertyValue(PROPERTY_VALUE)
                .isDeep(true)
                .build();

        // WHEN
        TranslationRulesService.IsNodeTranslatable result = rule.isTranslatable();

        // THEN
        assertEquals(TranslationRulesService.IsNodeTranslatable.NON_TRANSLATABLE, result);
    }

    @Test
    void shouldReturnOnlyChildrenTranslatableWhenIsDeepIsFalse() {
        // GIVEN
        TranslationNodeFilterRule rule = TranslationNodeFilterRule.builder()
                .propertyName(PROPERTY_NAME)
                .propertyValue(PROPERTY_VALUE)
                .isDeep(false)
                .build();

        // WHEN
        TranslationRulesService.IsNodeTranslatable result = rule.isTranslatable();

        // THEN
        assertEquals(TranslationRulesService.IsNodeTranslatable.ONLY_CHILDREN_TRANSLATABLE, result);
    }

}
