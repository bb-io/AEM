package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.dto.TranslationRules;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.services.impl.rules.AssetReferenceRule;
import io.blackbird.aemconnector.core.services.impl.rules.ContextRule;
import io.blackbird.aemconnector.core.services.impl.rules.TranslationRulesFileParser;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.Property;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.apache.jackrabbit.JcrConstants.JCR_CONTENT;
import static org.apache.jackrabbit.JcrConstants.JCR_DATA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationRulesServiceImplTest {

    @Mock
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @InjectMocks
    private TranslationRulesServiceImpl translationRulesService;

    @BeforeEach
    void setUp() {
        translationRulesService.activate();
    }

    @Test
    void shouldReturnTrueWhenPropertyIsTranslatable() throws BlackbirdInternalErrorException, LoginException {
        // GIVEN
        Property property = mock(Property.class);
        ContextRule contextRule = mock(ContextRule.class);
        List<ContextRule> contextRules = Collections.singletonList(contextRule);
        TranslationRules translationRules = new TranslationRules(contextRules, Collections.emptyList(), Collections.emptyList());

        try (MockedConstruction<TranslationRulesFileParser> ignored = Mockito.mockConstruction(
                TranslationRulesFileParser.class,
                (mock, context) -> when(mock.parse(any(InputStream.class))).thenReturn(translationRules))) {

            ResourceResolver resourceResolver = mockResourceResolver();
            when(serviceUserResolverProvider.getTranslationRulesReaderResolver()).thenReturn(resourceResolver);
            when(contextRule.appliesTo(property)).thenReturn(true);
            when(contextRule.isTranslatable(property)).thenReturn(true);

            // WHEN
            boolean result = translationRulesService.isTranslatable(property);

            // THEN
            assertTrue(result);
            verify(contextRule, times(1)).appliesTo(property);
            verify(contextRule, times(1)).isTranslatable(property);
        }
    }

    @Test
    void shouldReturnFalseWhenPropertyIsNotTranslatable() throws BlackbirdInternalErrorException, LoginException {
        // GIVEN
        Property property = mock(Property.class);
        ContextRule contextRule = mock(ContextRule.class);
        List<ContextRule> contextRules = Collections.singletonList(contextRule);
        TranslationRules translationRules = new TranslationRules(contextRules, Collections.emptyList(), Collections.emptyList());

        try (MockedConstruction<TranslationRulesFileParser> ignored = Mockito.mockConstruction(
                TranslationRulesFileParser.class,
                (mock, context) -> when(mock.parse(any(InputStream.class))).thenReturn(translationRules))) {

            ResourceResolver resourceResolver = mockResourceResolver();
            when(serviceUserResolverProvider.getTranslationRulesReaderResolver()).thenReturn(resourceResolver);
            when(contextRule.appliesTo(property)).thenReturn(true);
            when(contextRule.isTranslatable(property)).thenReturn(false);

            // WHEN
            boolean result = translationRulesService.isTranslatable(property);

            // THEN
            assertFalse(result);
            verify(contextRule, times(1)).appliesTo(property);
            verify(contextRule, times(1)).isTranslatable(property);
        }
    }

    @Test
    void shouldReturnFalseWhenNoRuleAppliesForProperty() throws BlackbirdInternalErrorException, LoginException {
        // GIVEN
        Property property = mock(Property.class);
        ContextRule contextRule = mock(ContextRule.class);
        List<ContextRule> contextRules = Collections.singletonList(contextRule);
        TranslationRules translationRules = new TranslationRules(contextRules, Collections.emptyList(), Collections.emptyList());

        try (MockedConstruction<TranslationRulesFileParser> ignored = Mockito.mockConstruction(
                TranslationRulesFileParser.class,
                (mock, context) -> when(mock.parse(any(InputStream.class))).thenReturn(translationRules))) {

            ResourceResolver resourceResolver = mockResourceResolver();
            when(serviceUserResolverProvider.getTranslationRulesReaderResolver()).thenReturn(resourceResolver);
            when(contextRule.appliesTo(property)).thenReturn(false);

            // WHEN
            boolean result = translationRulesService.isTranslatable(property);

            // THEN
            assertFalse(result);
            verify(contextRule, times(1)).appliesTo(property);
            verify(contextRule, times(0)).isTranslatable(property);
        }
    }

    @Test
    void shouldReturnNodeTranslatableWhenNodeIsTranslatable() throws BlackbirdInternalErrorException, LoginException {
        // GIVEN
        Node node = mock(Node.class);
        ContextRule contextRule = mock(ContextRule.class);
        List<ContextRule> contextRules = Collections.singletonList(contextRule);
        TranslationRules translationRules = new TranslationRules(contextRules, Collections.emptyList(), Collections.emptyList());

        try (MockedConstruction<TranslationRulesFileParser> ignored = Mockito.mockConstruction(
                TranslationRulesFileParser.class,
                (mock, context) -> when(mock.parse(any(InputStream.class))).thenReturn(translationRules))) {

            ResourceResolver resourceResolver = mockResourceResolver();
            when(serviceUserResolverProvider.getTranslationRulesReaderResolver()).thenReturn(resourceResolver);
            when(contextRule.appliesTo(node)).thenReturn(true);
            when(contextRule.isTranslatable(node)).thenReturn(TranslationRulesService.IsNodeTranslatable.TRANSLATABLE);

            // WHEN
            TranslationRulesService.IsNodeTranslatable result = translationRulesService.isTranslatable(node);

            // THEN
            assertEquals(TranslationRulesService.IsNodeTranslatable.TRANSLATABLE, result);
            verify(contextRule, times(1)).appliesTo(node);
            verify(contextRule, times(1)).isTranslatable(node);
        }
    }

    @Test
    void shouldReturnDefaultTranslatableWhenNoRuleAppliesForNode() throws BlackbirdInternalErrorException, LoginException {
        // GIVEN
        Node node = mock(Node.class);
        ContextRule contextRule = mock(ContextRule.class);
        List<ContextRule> contextRules = Collections.singletonList(contextRule);
        TranslationRules translationRules = new TranslationRules(contextRules, Collections.emptyList(), Collections.emptyList());

        try (MockedConstruction<TranslationRulesFileParser> ignored = Mockito.mockConstruction(
                TranslationRulesFileParser.class,
                (mock, context) -> when(mock.parse(any(InputStream.class))).thenReturn(translationRules))) {

            ResourceResolver resourceResolver = mockResourceResolver();
            when(serviceUserResolverProvider.getTranslationRulesReaderResolver()).thenReturn(resourceResolver);
            when(contextRule.appliesTo(node)).thenReturn(false);

            // WHEN
            TranslationRulesService.IsNodeTranslatable result = translationRulesService.isTranslatable(node);

            // THEN
            assertEquals(TranslationRulesService.IsNodeTranslatable.TRANSLATABLE, result);
            verify(contextRule, times(1)).appliesTo(node);
            verify(contextRule, times(0)).isTranslatable(node);
        }
    }

    @Test
    void shouldReturnAssetReferenceWhenPropertyIsAssetReference() throws BlackbirdInternalErrorException, LoginException {
        // GIVEN
        Property property = mock(Property.class);
        AssetReferenceRule assetReferenceRule = mock(AssetReferenceRule.class);
        List<AssetReferenceRule> assetReferenceRules = Collections.singletonList(assetReferenceRule);
        TranslationRules translationRules = new TranslationRules(Collections.emptyList(), assetReferenceRules, Collections.emptyList());

        try (MockedConstruction<TranslationRulesFileParser> ignored = Mockito.mockConstruction(
                TranslationRulesFileParser.class,
                (mock, context) -> when(mock.parse(any(InputStream.class))).thenReturn(translationRules))) {

            ResourceResolver resourceResolver = mockResourceResolver();
            when(serviceUserResolverProvider.getTranslationRulesReaderResolver()).thenReturn(resourceResolver);
            when(assetReferenceRule.isAssetReference(property)).thenReturn(TranslationRulesService.IsAssetReference.REFERENCE);

            // WHEN
            TranslationRulesService.IsAssetReference result = translationRulesService.isAssetReference(property);

            // THEN
            assertEquals(TranslationRulesService.IsAssetReference.REFERENCE, result);
            verify(assetReferenceRule, times(1)).isAssetReference(property);
        }
    }

    @Test
    void shouldReturnNotReferenceWhenPropertyIsNotAssetReference() throws BlackbirdInternalErrorException, LoginException {
        // GIVEN
        Property property = mock(Property.class);
        AssetReferenceRule assetReferenceRule = mock(AssetReferenceRule.class);
        List<AssetReferenceRule> assetReferenceRules = Collections.singletonList(assetReferenceRule);
        TranslationRules translationRules = new TranslationRules(Collections.emptyList(), assetReferenceRules, Collections.emptyList());

        try (MockedConstruction<TranslationRulesFileParser> ignored = Mockito.mockConstruction(
                TranslationRulesFileParser.class,
                (mock, context) -> when(mock.parse(any(InputStream.class))).thenReturn(translationRules))) {

            ResourceResolver resourceResolver = mockResourceResolver();
            when(serviceUserResolverProvider.getTranslationRulesReaderResolver()).thenReturn(resourceResolver);
            when(assetReferenceRule.isAssetReference(property)).thenReturn(null);

            // WHEN
            TranslationRulesService.IsAssetReference result = translationRulesService.isAssetReference(property);

            // THEN
            assertEquals(TranslationRulesService.IsAssetReference.NOT_REFERENCE, result);
            verify(assetReferenceRule, times(1)).isAssetReference(property);
        }
    }

    @Test
    void shouldThrowExceptionWhenNoTranslationRulesFound() throws LoginException {
        // GIVEN
        Property property = mock(Property.class);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        when(serviceUserResolverProvider.getTranslationRulesReaderResolver()).thenReturn(resourceResolver);
        when(resourceResolver.getResource(anyString())).thenReturn(null);

        // WHEN & THEN
        assertThrows(BlackbirdInternalErrorException.class, () -> translationRulesService.isTranslatable(property));
    }

    @Test
    void shouldReturnEmptyRulesWhenInputStreamThrowsIOException() throws LoginException {
        // GIVEN
        Property property = mock(Property.class);
        when(serviceUserResolverProvider.getTranslationRulesReaderResolver()).thenThrow(LoginException.class);

        // WHEN
        translationRulesService.activate();

        // THEN
        assertThrows(BlackbirdInternalErrorException.class, () -> translationRulesService.isAssetReference(property));
    }

    private ResourceResolver mockResourceResolver() {
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        Resource confResource = mock(Resource.class);
        Resource jcrContentResource = mock(Resource.class);
        ValueMap valueMap = mock(ValueMap.class);
        InputStream inputStream = mock(InputStream.class);

        when(resourceResolver.getResource("/conf/global/settings/translation/rules/translation_rules.xml")).thenReturn(confResource);
        when(confResource.getChild(JCR_CONTENT)).thenReturn(jcrContentResource);
        when(jcrContentResource.getValueMap()).thenReturn(valueMap);
        when(valueMap.get(JCR_DATA, InputStream.class)).thenReturn(inputStream);

        return resourceResolver;
    }

}
