package io.blackbird.aemconnector.core.services.impl;

import com.adobe.granite.asset.api.AssetManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlackbirdCfCopyMergeServiceImplTest {

    @Mock
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private Resource sourceResource;

    @Mock
    private Resource targetResource;

    @Mock
    private Resource targetJcrContent;

    @Mock
    private Resource sourceJcrContent;

    @Mock
    private Resource parentTargetResource;

    @Mock
    private AssetManager assetManager;

    @InjectMocks
    private BlackbirdCfCopyMergeServiceImpl service;

    private String sourcePath;
    private String targetPath;
    private JsonNode targetContent;

    @BeforeEach
    void setUp() throws Exception {
        sourcePath = "/content/bb-aem-connector/cf/us/en/source";
        targetPath = "/content/bb-aem-connector/cf/pl/pl/target";
        targetContent = new ObjectMapper().createObjectNode();

        when(serviceUserResolverProvider.getTranslationWriterResolver()).thenReturn(resolver);
    }

    @Test
    void shouldCreateResourceWhenTargetResourceDoesNotExist() throws Exception {
        when(resolver.getResource(sourcePath)).thenReturn(sourceResource);
        when(sourceResource.getPath()).thenReturn("/content/bb-aem-connector/cf/us/en/source");
        when(resolver.getResource(targetPath)).thenReturn(null).thenReturn(targetResource);
        when(resolver.getResource("/content/bb-aem-connector/cf/pl/pl")).thenReturn(parentTargetResource);
        when(resolver.adaptTo(AssetManager.class)).thenReturn(assetManager);

        Resource result = service.copyAndMerge(sourcePath, targetPath, targetContent, null);

        verify(resolver).getResource(sourcePath);
        verify(resolver, times(2)).getResource(targetPath);
        verify(assetManager).copyAsset(sourceResource.getPath(), targetPath);
        verify(resolver, times(2)).commit();
        assertNotNull(result);
    }

    @Test
    void shouldReplaceExistingTargetResourceWithNewCopyWhenTargetResourceExist() throws Exception {
        when(resolver.getResource(sourcePath)).thenReturn(sourceResource);
        when(resolver.getResource(targetPath)).thenReturn(targetResource);
        when(targetResource.getPath()).thenReturn(targetPath);
        when(targetResource.getChild("jcr:content")).thenReturn(targetJcrContent);
        when(sourceResource.getChild("jcr:content")).thenReturn(sourceJcrContent);

        Resource result = service.copyAndMerge(sourcePath, targetPath, targetContent, null);

        verify(resolver).getResource(sourcePath);
        verify(resolver, times(2)).getResource(targetPath);
        verify(resolver).delete(targetJcrContent);
        verify(resolver).copy(sourceResource.getPath(), "/content/bb-aem-connector/cf/pl/pl/target");
        verify(resolver, times(2)).commit();
        assertNotNull(result);
    }

    @Test
    void shouldThrowExceptionWhenSourceResourceDoesNotExist() throws Exception {
        when(resolver.getResource(sourcePath)).thenReturn(null);

        BlackbirdResourceCopyMergeException exception = assertThrows(BlackbirdResourceCopyMergeException.class,
                () -> service.copyAndMerge(sourcePath, targetPath, targetContent, null));

        assertEquals("Source resource does not exist, /content/bb-aem-connector/cf/us/en/source", exception.getMessage());
    }
}

