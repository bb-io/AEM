package io.blackbird.aemconnector.core.services.impl.rules;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.utils.RepositoryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.Property;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceTypeRuleTest {

    @Test
    void shouldReturnTrueWhenPropertyMatchesResourceTypeAndPropertyRule() throws BlackbirdInternalErrorException {
        // GIVEN
        String resourceType = "test/resource/type";
        Property property = mock(Property.class);
        Node resourceTypeNode = mock(Node.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = Collections.singletonList(propertyRule);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getParentWithProperty(property, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceTypeNode);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(resourceTypeNode, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceType);

            when(propertyRule.appliesTo(property)).thenReturn(true);

            ResourceTypeRule rule = ResourceTypeRule.builder()
                    .resourceType(resourceType)
                    .propertyNameRules(propertyRules)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertTrue(result);
        }
    }

    @Test
    void shouldReturnFalseWhenResourceTypeDoesNotMatch() throws BlackbirdInternalErrorException {
        // GIVEN
        String resourceType = "test/resource/type";
        Property property = mock(Property.class);
        Node resourceTypeNode = mock(Node.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = Collections.singletonList(propertyRule);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getParentWithProperty(property, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceTypeNode);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(resourceTypeNode, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn("different/resource/type");

            ResourceTypeRule rule = ResourceTypeRule.builder()
                    .resourceType(resourceType)
                    .propertyNameRules(propertyRules)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnFalseWhenResourceTypeNodeIsNull() throws BlackbirdInternalErrorException {
        // GIVEN
        String resourceType = "test/resource/type";
        Property property = mock(Property.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = Collections.singletonList(propertyRule);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getParentWithProperty(property, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(null);

            ResourceTypeRule rule = ResourceTypeRule.builder()
                    .resourceType(resourceType)
                    .propertyNameRules(propertyRules)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnFalseWhenNoPropertyRuleApplies() throws BlackbirdInternalErrorException {
        // GIVEN
        String resourceType = "test/resource/type";
        Property property = mock(Property.class);
        Node resourceTypeNode = mock(Node.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = Collections.singletonList(propertyRule);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getParentWithProperty(property, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceTypeNode);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(resourceTypeNode, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceType);

            when(propertyRule.appliesTo(property)).thenReturn(false);

            ResourceTypeRule rule = ResourceTypeRule.builder()
                    .resourceType(resourceType)
                    .propertyNameRules(propertyRules)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnTrueForIsTranslatableWhenPropertyRuleApplies() throws BlackbirdInternalErrorException {
        // GIVEN
        String resourceType = "test/resource/type";
        Property property = mock(Property.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = Collections.singletonList(propertyRule);

        when(propertyRule.appliesTo(property)).thenReturn(true);
        when(propertyRule.isTranslatable(property)).thenReturn(true);

        ResourceTypeRule rule = ResourceTypeRule.builder()
                .resourceType(resourceType)
                .propertyNameRules(propertyRules)
                .build();

        // WHEN
        boolean result = rule.isTranslatable(property);

        // THEN
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseForIsTranslatableWhenPropertyRuleAppliesToFalse() throws BlackbirdInternalErrorException {
        // GIVEN
        String resourceType = "test/resource/type";
        Property property = mock(Property.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = Collections.singletonList(propertyRule);

        when(propertyRule.appliesTo(property)).thenReturn(false);

        ResourceTypeRule rule = ResourceTypeRule.builder()
                .resourceType(resourceType)
                .propertyNameRules(propertyRules)
                .build();

        // WHEN
        boolean result = rule.isTranslatable(property);

        // THEN
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForIsTranslatableWhenPropertyRuleIsTranslatableFalse() throws BlackbirdInternalErrorException {
        // GIVEN
        String resourceType = "test/resource/type";
        Property property = mock(Property.class);
        TranslationPropertyRule propertyRule = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = Collections.singletonList(propertyRule);

        when(propertyRule.appliesTo(property)).thenReturn(true);
        when(propertyRule.isTranslatable(property)).thenReturn(false);

        ResourceTypeRule rule = ResourceTypeRule.builder()
                .resourceType(resourceType)
                .propertyNameRules(propertyRules)
                .build();

        // WHEN
        boolean result = rule.isTranslatable(property);

        // THEN
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForIsTranslatableWhenNoPropertyRuleApplies() throws BlackbirdInternalErrorException {
        // GIVEN
        String resourceType = "test/resource/type";
        Property property = mock(Property.class);
        TranslationPropertyRule propertyRule1 = mock(TranslationPropertyRule.class);
        TranslationPropertyRule propertyRule2 = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = Arrays.asList(propertyRule1, propertyRule2);

        when(propertyRule1.appliesTo(property)).thenReturn(false);
        when(propertyRule2.appliesTo(property)).thenReturn(false);

        ResourceTypeRule rule = ResourceTypeRule.builder()
                .resourceType(resourceType)
                .propertyNameRules(propertyRules)
                .build();

        // WHEN
        boolean result = rule.isTranslatable(property);

        // THEN
        assertFalse(result);
    }

    @Test
    void shouldHandleMultiplePropertyRules() throws BlackbirdInternalErrorException {
        // GIVEN
        String resourceType = "test/resource/type";
        Property property = mock(Property.class);
        Node resourceTypeNode = mock(Node.class);
        TranslationPropertyRule propertyRule1 = mock(TranslationPropertyRule.class);
        TranslationPropertyRule propertyRule2 = mock(TranslationPropertyRule.class);
        List<TranslationPropertyRule> propertyRules = Arrays.asList(propertyRule1, propertyRule2);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getParentWithProperty(property, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceTypeNode);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(resourceTypeNode, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceType);

            when(propertyRule1.appliesTo(property)).thenReturn(false);
            when(propertyRule2.appliesTo(property)).thenReturn(true);

            ResourceTypeRule rule = ResourceTypeRule.builder()
                    .resourceType(resourceType)
                    .propertyNameRules(propertyRules)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertTrue(result);
        }
    }

    @Test
    void shouldHandleEmptyPropertyRulesList() throws BlackbirdInternalErrorException {
        // GIVEN
        String resourceType = "test/resource/type";
        Property property = mock(Property.class);
        Node resourceTypeNode = mock(Node.class);
        List<TranslationPropertyRule> propertyRules = Collections.emptyList();

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getParentWithProperty(property, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceTypeNode);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(resourceTypeNode, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceType);

            ResourceTypeRule rule = ResourceTypeRule.builder()
                    .resourceType(resourceType)
                    .propertyNameRules(propertyRules)
                    .build();

            // WHEN
            boolean result = rule.appliesTo(property);

            // THEN
            assertFalse(result);
        }
    }

}
