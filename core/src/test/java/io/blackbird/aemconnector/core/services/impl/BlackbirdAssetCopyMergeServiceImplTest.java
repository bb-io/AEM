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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.nodetype.NodeType;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlackbirdAssetCopyMergeServiceImplTest {

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

    @Mock
    private Node sourceJcrContentNode;

    @Mock
    private Node targetNode;

    @Mock
    private NodeType nodeType;

    @Mock
    private PropertyIterator propertyIterator;

    @Mock
    private NodeIterator nodeIterator;

    @InjectMocks
    private BlackbirdAssetCopyMergeServiceImpl service;

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
        when(sourceJcrContent.adaptTo(Node.class)).thenReturn(sourceJcrContentNode);
        when(targetResource.adaptTo(Node.class)).thenReturn(targetNode);
        when(sourceJcrContentNode.getPrimaryNodeType()).thenReturn(nodeType);
        when(nodeType.getName()).thenReturn("dam:AssetContent");
        when(sourceJcrContentNode.getMixinNodeTypes()).thenReturn(new NodeType [0]);
        when(sourceJcrContentNode.getProperties()).thenReturn(propertyIterator);
        when(propertyIterator.hasNext()).thenReturn(false).thenReturn(false);
        when(sourceJcrContentNode.getNodes()).thenReturn(nodeIterator);
        when(nodeIterator.hasNext()).thenReturn(false);

        Resource result = service.copyAndMerge(sourcePath, targetPath, targetContent, null);

        verify(resolver).getResource(sourcePath);
        verify(resolver, times(2)).getResource(targetPath);
        verify(resolver).delete(targetJcrContent);
        verify(resolver).commit();
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

