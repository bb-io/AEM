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

import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AssetReferenceRuleTest {

    @Test
    void shouldReturnReferenceWhenPropertyMatchesCriteria() throws BlackbirdInternalErrorException {
        // GIVEN
        String assetReferenceAttribute = "fileReference";
        String resourceType = "dam/components/image";
        boolean checkInChildNodes = false;
        boolean createLangCopy = false;

        Property property = mock(Property.class);
        Node parentNode = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getParent(property)).thenReturn(parentNode);
            repositoryUtils.when(() -> RepositoryUtils.hasProperty(parentNode, SLING_RESOURCE_TYPE_PROPERTY)).thenReturn(true);
            repositoryUtils.when(() -> RepositoryUtils.getName(property)).thenReturn(assetReferenceAttribute);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(parentNode, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceType);

            AssetReferenceRule rule = AssetReferenceRule.builder()
                    .assetReferenceAttribute(assetReferenceAttribute)
                    .resourceType(resourceType)
                    .checkInChildNodes(checkInChildNodes)
                    .createLangCopy(createLangCopy)
                    .build();

            // WHEN
            TranslationRulesService.IsAssetReference result = rule.isAssetReference(property);

            // THEN
            assertEquals(TranslationRulesService.IsAssetReference.REFERENCE, result);
        }
    }

    @Test
    void shouldReturnReferenceWithChildrenWhenPropertyMatchesCriteriaWithChildNodes() throws BlackbirdInternalErrorException {
        // GIVEN
        String assetReferenceAttribute = "fileReference";
        String resourceType = "dam/components/image";
        boolean checkInChildNodes = true;
        boolean createLangCopy = false;

        Property property = mock(Property.class);
        Node parentNode = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getParent(property)).thenReturn(parentNode);
            repositoryUtils.when(() -> RepositoryUtils.hasProperty(parentNode, SLING_RESOURCE_TYPE_PROPERTY)).thenReturn(true);
            repositoryUtils.when(() -> RepositoryUtils.getName(property)).thenReturn(assetReferenceAttribute);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(parentNode, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceType);

            AssetReferenceRule rule = AssetReferenceRule.builder()
                    .assetReferenceAttribute(assetReferenceAttribute)
                    .resourceType(resourceType)
                    .checkInChildNodes(checkInChildNodes)
                    .createLangCopy(createLangCopy)
                    .build();

            // WHEN
            TranslationRulesService.IsAssetReference result = rule.isAssetReference(property);

            // THEN
            assertEquals(TranslationRulesService.IsAssetReference.REFERENCE_WITH_CHILDREN, result);
        }
    }

    @Test
    void shouldReturnNullWhenPropertyNameDoesNotMatch() throws BlackbirdInternalErrorException {
        // GIVEN
        String assetReferenceAttribute = "fileReference";
        String resourceType = "dam/components/image";
        boolean checkInChildNodes = false;
        boolean createLangCopy = false;

        Property property = mock(Property.class);
        Node parentNode = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getParent(property)).thenReturn(parentNode);
            repositoryUtils.when(() -> RepositoryUtils.getName(property)).thenReturn("differentProperty");
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(parentNode, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn(resourceType);

            AssetReferenceRule rule = AssetReferenceRule.builder()
                    .assetReferenceAttribute(assetReferenceAttribute)
                    .resourceType(resourceType)
                    .checkInChildNodes(checkInChildNodes)
                    .createLangCopy(createLangCopy)
                    .build();

            // WHEN
            TranslationRulesService.IsAssetReference result = rule.isAssetReference(property);

            // THEN
            assertNull(result);
        }
    }

    @Test
    void shouldReturnNullWhenResourceTypeDoesNotMatch() throws BlackbirdInternalErrorException {
        // GIVEN
        String assetReferenceAttribute = "fileReference";
        String resourceType = "dam/components/image";
        boolean checkInChildNodes = false;
        boolean createLangCopy = false;

        Property property = mock(Property.class);
        Node parentNode = mock(Node.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getParent(property)).thenReturn(parentNode);
            repositoryUtils.when(() -> RepositoryUtils.getName(property)).thenReturn(assetReferenceAttribute);
            repositoryUtils.when(() -> RepositoryUtils.getPropertyAsString(parentNode, SLING_RESOURCE_TYPE_PROPERTY))
                    .thenReturn("different/resource/type");
            repositoryUtils.when(() -> RepositoryUtils.getPrimaryNodeTypeAsString(parentNode)).thenReturn("nt:unstructured");

            AssetReferenceRule rule = AssetReferenceRule.builder()
                    .assetReferenceAttribute(assetReferenceAttribute)
                    .resourceType(resourceType)
                    .checkInChildNodes(checkInChildNodes)
                    .createLangCopy(createLangCopy)
                    .build();

            // WHEN
            TranslationRulesService.IsAssetReference result = rule.isAssetReference(property);

            // THEN
            assertNull(result);
        }
    }

    @Test
    void shouldThrowExceptionWhenParentNodeIsNull() {
        // GIVEN
        String assetReferenceAttribute = "fileReference";
        String resourceType = "dam/components/image";
        boolean checkInChildNodes = false;
        boolean createLangCopy = false;

        Property property = mock(Property.class);

        try (MockedStatic<RepositoryUtils> repositoryUtils = Mockito.mockStatic(RepositoryUtils.class)) {
            repositoryUtils.when(() -> RepositoryUtils.getParent(property)).thenReturn(null);
            repositoryUtils.when(() -> RepositoryUtils.getPath(property)).thenReturn("/content/path/to/property");

            AssetReferenceRule rule = AssetReferenceRule.builder()
                    .assetReferenceAttribute(assetReferenceAttribute)
                    .resourceType(resourceType)
                    .checkInChildNodes(checkInChildNodes)
                    .createLangCopy(createLangCopy)
                    .build();

            // WHEN & THEN
            assertThrows(BlackbirdInternalErrorException.class, () -> rule.isAssetReference(property));
        }
    }

}
