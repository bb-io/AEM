package io.blackbird.aemconnector.core.services.impl;

import com.adobe.granite.asset.api.AssetManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class DitaCopyMergeServiceImplTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    @Mock
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Mock
    private AssetManager mockAssetManager;

    private ResourceResolver spyResolver;

    private DitaCopyMergeServiceImpl target;

    @BeforeEach
    void setUp() throws LoginException, BlackbirdInternalErrorException {
        context.load().json("/content/base-content.json", "/content");

        spyResolver = Mockito.spy(context.resourceResolver());
        Mockito.doNothing().when(spyResolver).close();

        context.registerService(BlackbirdServiceUserResolverProvider.class, serviceUserResolverProvider);
        when(serviceUserResolverProvider.getTranslationWriterResolver()).thenReturn(spyResolver);

        target = context.registerInjectActivateService(new DitaCopyMergeServiceImpl());
    }

    @Test
    void shouldReplaceExistingDitaWithNewCopyWhenTargetDitaExist() throws Exception {
        String sourcePath = "/content/dita/en/source.dita";
        String targetPath = "/content/dita/pl/target.dita";
        JsonNode targetContent = new ObjectMapper().createObjectNode();

        Resource result = target.copyAndMerge(sourcePath, targetPath, targetContent, null);

        verify(spyResolver).getResource(sourcePath);
        verify(spyResolver, times(2)).getResource(targetPath);
        verify(spyResolver).commit();
        assertNotNull(result);
    }

    @Test
    void shouldCreateDitaWhenTargetDitaDoesNotExist() throws Exception {
        String sourcePath = "/content/dita/en/source.dita";
        String targetPath = "/content/dita/ca/target.dita";
        JsonNode targetContent = new ObjectMapper().createObjectNode();

        Resource jcrContentResource = mock(Resource.class);
        Resource updatedResource = mock(Resource.class);
        when(spyResolver.adaptTo(AssetManager.class)).thenReturn(mockAssetManager);

        AtomicInteger targetCallCount = new AtomicInteger();
        doAnswer(invocation -> {
            String path = invocation.getArgument(0);
            if (targetPath.equals(path) && targetCallCount.incrementAndGet() == 2) {
                return updatedResource;
            }
            if ("/content/dita/ca/target.dita/jcr:content".equals(path) || "/content/dita/ca/target.dita/jcr:content/renditions/original/jcr:content".equals(path)) {
                return jcrContentResource;
            }
            return invocation.callRealMethod();
        }).when(spyResolver).getResource(anyString());
        when(jcrContentResource.adaptTo(ModifiableValueMap.class)).thenReturn(mock(ModifiableValueMap.class));

        Resource result = target.copyAndMerge(sourcePath, targetPath, targetContent, null);

        verify(spyResolver).getResource(sourcePath);
        verify(spyResolver, times(2)).getResource(targetPath);
        verify(spyResolver, times(2)).commit();
        assertNotNull(result);
    }

    @Test
    void shouldThrowExceptionWhenSourceResourceDoesNotExist() {
        String sourcePath = "/content/dita/en/source-does-not-exist.dita";
        String targetPath = "/content/dita/pl/target-does-not-exist.dita";
        JsonNode targetContent = new ObjectMapper().createObjectNode();

        BlackbirdResourceCopyMergeException exception = assertThrows(BlackbirdResourceCopyMergeException.class,
                () -> target.copyAndMerge(sourcePath, targetPath, targetContent, null));

        assertEquals("Source resource does not exist, /content/dita/en/source-does-not-exist.dita", exception.getMessage());
    }
}

