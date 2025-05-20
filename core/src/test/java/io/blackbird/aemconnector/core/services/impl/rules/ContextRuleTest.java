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
import javax.jcr.Property;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextRuleTest {

    private static final String CONTEXT_PATH = "/content/test";
    private static final String PROPERTY_PATH = "/content/test/page/jcr:content/property";
    private static final String NODE_PATH = "/content/test/page/jcr:content";
    private static final String NON_CONTEXT_PATH = "/content/other/page/jcr:content/property";

    @Test
    void shouldReturnTrueWhenPropertyIsInContextPath() throws BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn(PROPERTY_PATH);

            ContextRule rule = ContextRule.builder()
                    .contextPath(CONTEXT_PATH)
                    .translationNodeFilterRules(Collections.emptyList())
                    .propertyFilterRules(Collections.emptyList())
                    .resourceTypeRules(Collections.emptyList())
                    .generalRules(Collections.emptyList())
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertTrue(result);
        }
    }

    @Test
    void shouldReturnFalseWhenPropertyIsNotInContextPath() throws BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn(NON_CONTEXT_PATH);

            ContextRule rule = ContextRule.builder()
                    .contextPath(CONTEXT_PATH)
                    .translationNodeFilterRules(Collections.emptyList())
                    .propertyFilterRules(Collections.emptyList())
                    .resourceTypeRules(Collections.emptyList())
                    .generalRules(Collections.emptyList())
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnTrueWhenPropertyIsTranslatableByPropertyFilterRule() throws BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = new ArrayList<>();
        propertyRules.add(propertyRule);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn(PROPERTY_PATH);
            when(propertyRule.appliesTo(property)).thenReturn(true);
            when(propertyRule.isTranslatable(property)).thenReturn(true);

            ContextRule rule = ContextRule.builder()
                    .contextPath(CONTEXT_PATH)
                    .translationNodeFilterRules(Collections.emptyList())
                    .propertyFilterRules(propertyRules)
                    .resourceTypeRules(Collections.emptyList())
                    .generalRules(Collections.emptyList())
                    .build();

            // WHEN
            boolean result = rule.isTranslatable(property);

            // THEN
            assertTrue(result);
        }
    }

    @Test
    void shouldReturnFalseWhenPropertyIsNotTranslatableByPropertyFilterRule() throws BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = new ArrayList<>();
        propertyRules.add(propertyRule);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn(PROPERTY_PATH);
            when(propertyRule.appliesTo(property)).thenReturn(true);
            when(propertyRule.isTranslatable(property)).thenReturn(false);

            ContextRule rule = ContextRule.builder()
                    .contextPath(CONTEXT_PATH)
                    .translationNodeFilterRules(Collections.emptyList())
                    .propertyFilterRules(propertyRules)
                    .resourceTypeRules(Collections.emptyList())
                    .generalRules(Collections.emptyList())
                    .build();

            // WHEN
            boolean result = rule.isTranslatable(property);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldCheckResourceTypeRulesWhenPropertyFilterRulesDoNotApply() throws BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        TranslationPropertyRule resourceTypeRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = new ArrayList<>();
        List<TranslationPropertyRule> resourceTypeRules = new ArrayList<>();
        propertyRules.add(propertyRule);
        resourceTypeRules.add(resourceTypeRule);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn(PROPERTY_PATH);
            when(propertyRule.appliesTo(property)).thenReturn(false);
            when(resourceTypeRule.appliesTo(property)).thenReturn(true);
            when(resourceTypeRule.isTranslatable(property)).thenReturn(true);

            ContextRule rule = ContextRule.builder()
                    .contextPath(CONTEXT_PATH)
                    .translationNodeFilterRules(Collections.emptyList())
                    .propertyFilterRules(propertyRules)
                    .resourceTypeRules(resourceTypeRules)
                    .generalRules(Collections.emptyList())
                    .build();

            // WHEN
            boolean result = rule.isTranslatable(property);

            // THEN
            assertTrue(result);
        }
    }

    @Test
    void shouldCheckGeneralRulesWhenOtherRulesDoNotApply() throws BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        TranslationPropertyRule resourceTypeRule = mock(TranslationPropertyRule.class);
        TranslationPropertyRule generalRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = new ArrayList<>();
        List<TranslationPropertyRule> resourceTypeRules = new ArrayList<>();
        List<TranslationPropertyRule> generalRules = new ArrayList<>();
        propertyRules.add(propertyRule);
        resourceTypeRules.add(resourceTypeRule);
        generalRules.add(generalRule);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn(PROPERTY_PATH);
            when(propertyRule.appliesTo(property)).thenReturn(false);
            when(resourceTypeRule.appliesTo(property)).thenReturn(false);
            when(generalRule.appliesTo(property)).thenReturn(true);
            when(generalRule.isTranslatable(property)).thenReturn(true);

            ContextRule rule = ContextRule.builder()
                    .contextPath(CONTEXT_PATH)
                    .translationNodeFilterRules(Collections.emptyList())
                    .propertyFilterRules(propertyRules)
                    .resourceTypeRules(resourceTypeRules)
                    .generalRules(generalRules)
                    .build();

            // WHEN
            boolean result = rule.isTranslatable(property);

            // THEN
            assertTrue(result);
        }
    }

    @Test
    void shouldReturnFalseWhenNoRulesApply() throws BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        TranslationPropertyRule resourceTypeRule = mock(TranslationPropertyRule.class);
        TranslationPropertyRule generalRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = new ArrayList<>();
        List<TranslationPropertyRule> resourceTypeRules = new ArrayList<>();
        List<TranslationPropertyRule> generalRules = new ArrayList<>();
        propertyRules.add(propertyRule);
        resourceTypeRules.add(resourceTypeRule);
        generalRules.add(generalRule);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn(PROPERTY_PATH);
            when(propertyRule.appliesTo(property)).thenReturn(false);
            when(resourceTypeRule.appliesTo(property)).thenReturn(false);
            when(generalRule.appliesTo(property)).thenReturn(false);

            ContextRule rule = ContextRule.builder()
                    .contextPath(CONTEXT_PATH)
                    .translationNodeFilterRules(Collections.emptyList())
                    .propertyFilterRules(propertyRules)
                    .resourceTypeRules(resourceTypeRules)
                    .generalRules(generalRules)
                    .build();

            // WHEN
            boolean result = rule.isTranslatable(property);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnTrueWhenNodeIsInContextPath() throws BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(node)).thenReturn(NODE_PATH);

            ContextRule rule = ContextRule.builder()
                    .contextPath(CONTEXT_PATH)
                    .translationNodeFilterRules(Collections.emptyList())
                    .propertyFilterRules(Collections.emptyList())
                    .resourceTypeRules(Collections.emptyList())
                    .generalRules(Collections.emptyList())
                    .build();

            // WHEN
            boolean result = rule.appliesTo(node);

            // THEN
            assertTrue(result);
        }
    }

    @Test
    void shouldReturnFalseWhenNodeIsNotInContextPath() throws BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(node)).thenReturn(NON_CONTEXT_PATH);

            ContextRule rule = ContextRule.builder()
                    .contextPath(CONTEXT_PATH)
                    .translationNodeFilterRules(Collections.emptyList())
                    .propertyFilterRules(Collections.emptyList())
                    .resourceTypeRules(Collections.emptyList())
                    .generalRules(Collections.emptyList())
                    .build();

            // WHEN
            boolean result = rule.appliesTo(node);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnNodeFilterRuleResultWhenRuleApplies() throws BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);
        TranslationNodeFilterRule nodeRule = mock(TranslationNodeFilterRule.class);
        List<TranslationNodeFilterRule> nodeRules = new ArrayList<>();
        nodeRules.add(nodeRule);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(node)).thenReturn(NODE_PATH);
            when(nodeRule.appliesTo(node)).thenReturn(true);
            when(nodeRule.isTranslatable()).thenReturn(TranslationRulesService.IsNodeTranslatable.NON_TRANSLATABLE);

            ContextRule rule = ContextRule.builder()
                    .contextPath(CONTEXT_PATH)
                    .translationNodeFilterRules(nodeRules)
                    .propertyFilterRules(Collections.emptyList())
                    .resourceTypeRules(Collections.emptyList())
                    .generalRules(Collections.emptyList())
                    .build();

            // WHEN
            TranslationRulesService.IsNodeTranslatable result = rule.isTranslatable(node);

            // THEN
            assertEquals(TranslationRulesService.IsNodeTranslatable.NON_TRANSLATABLE, result);
        }
    }

    @Test
    void shouldReturnTranslatableWhenNoNodeFilterRuleApplies() throws BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);
        TranslationNodeFilterRule nodeRule = mock(TranslationNodeFilterRule.class);
        List<TranslationNodeFilterRule> nodeRules = new ArrayList<>();
        nodeRules.add(nodeRule);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getPath(node)).thenReturn(NODE_PATH);
            when(nodeRule.appliesTo(node)).thenReturn(false);

            ContextRule rule = ContextRule.builder()
                    .contextPath(CONTEXT_PATH)
                    .translationNodeFilterRules(nodeRules)
                    .propertyFilterRules(Collections.emptyList())
                    .resourceTypeRules(Collections.emptyList())
                    .generalRules(Collections.emptyList())
                    .build();

            // WHEN
            TranslationRulesService.IsNodeTranslatable result = rule.isTranslatable(node);

            // THEN
            assertEquals(TranslationRulesService.IsNodeTranslatable.TRANSLATABLE, result);
        }
    }

}
