package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.objects.VersionSyncResult;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersioningServiceImplTest {

    @Mock
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private Session session;

    @Mock
    private Workspace workspace;

    @Mock
    private VersionManager versionManager;

    @InjectMocks
    private VersioningServiceImpl versioningService;

    private void mockVersionManagerChain() throws RepositoryException, LoginException {
        when(serviceUserResolverProvider.getTranslationWriterResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(session);
        when(session.getWorkspace()).thenReturn(workspace);
        when(workspace.getVersionManager()).thenReturn(versionManager);
    }

    @Test
    void synchronizeVersionShouldCreateMissingVersionsAndReturnResult() throws Exception {

        String sourcePath = "/content/source";
        String targetPath = "/content/target";

        mockVersionManagerChain();

        Version sourceBaseVersion = mock(Version.class);
        Version targetBaseVersion = mock(Version.class);

        when(versionManager.getBaseVersion(sourcePath)).thenReturn(sourceBaseVersion);
        when(versionManager.getBaseVersion(targetPath)).thenReturn(targetBaseVersion);

        when(sourceBaseVersion.getName()).thenReturn("1.3");
        when(targetBaseVersion.getName()).thenReturn("1.1");

        VersionHistory sourceHistory = mock(VersionHistory.class);
        VersionIterator sourceIterator = mock(VersionIterator.class);
        when(versionManager.getVersionHistory(sourcePath)).thenReturn(sourceHistory);
        when(sourceHistory.getAllVersions()).thenReturn(sourceIterator);
        when(sourceIterator.hasNext()).thenReturn(true, true, true, false);
        when(sourceIterator.nextVersion())
                .thenReturn(mock(Version.class), mock(Version.class), mock(Version.class));

        VersionHistory targetHistory = mock(VersionHistory.class);
        VersionIterator targetIterator = mock(VersionIterator.class);
        when(versionManager.getVersionHistory(targetPath)).thenReturn(targetHistory);
        when(targetHistory.getAllVersions()).thenReturn(targetIterator);
        when(targetIterator.hasNext()).thenReturn(true, false);
        when(targetIterator.nextVersion()).thenReturn(mock(Version.class));

        Version checkpoint1 = mock(Version.class);
        Version checkpoint2 = mock(Version.class);
        when(versionManager.checkpoint(targetPath)).thenReturn(checkpoint1, checkpoint2);
        when(checkpoint1.getName()).thenReturn("1.2");
        when(checkpoint2.getName()).thenReturn("1.3");

        VersionSyncResult result = versioningService.synchronizeVersion(sourcePath, targetPath);

        assertNotNull(result);
        assertEquals("1.3", result.getSourceVersion());
        assertEquals("1.1", result.getTargetOldVersion());
        assertEquals("1.3", result.getTargetNewVersion());


        verify(versionManager, times(2)).checkpoint(targetPath);

        verify(resolver, times(1)).close();
    }

    @Test
    void getVersionCountDifferenceShouldReturnCorrectDifference() throws Exception {

        String sourcePath = "/content/source";
        String targetPath = "/content/target";

        mockVersionManagerChain();

        VersionHistory sourceHistory = mock(VersionHistory.class);
        VersionIterator sourceIterator = mock(VersionIterator.class);
        when(versionManager.getVersionHistory(sourcePath)).thenReturn(sourceHistory);
        when(sourceHistory.getAllVersions()).thenReturn(sourceIterator);
        when(sourceIterator.hasNext()).thenReturn(true, true, true, true, false);
        when(sourceIterator.nextVersion())
                .thenReturn(mock(Version.class),
                        mock(Version.class),
                        mock(Version.class),
                        mock(Version.class));

        VersionHistory targetHistory = mock(VersionHistory.class);
        VersionIterator targetIterator = mock(VersionIterator.class);
        when(versionManager.getVersionHistory(targetPath)).thenReturn(targetHistory);
        when(targetHistory.getAllVersions()).thenReturn(targetIterator);
        when(targetIterator.hasNext()).thenReturn(true, true, false);
        when(targetIterator.nextVersion())
                .thenReturn(mock(Version.class), mock(Version.class));


        int diff = versioningService.getVersionCountDifference(sourcePath, targetPath);

        assertEquals(2, diff);
        verify(resolver, times(1)).close();
    }

    @Test
    void getVersionCountDifferenceShouldThrowOnLoginException() throws Exception {

        when(serviceUserResolverProvider.getTranslationWriterResolver())
                .thenThrow(new LoginException("login failed"));

        BlackbirdServiceException ex = assertThrows(
                BlackbirdServiceException.class,
                () -> versioningService.getVersionCountDifference("/content/source", "/content/target")
        );

        assertEquals("login failed", ex.getMessage());
    }

    @Test
    void synchronizeVersionShouldThrowOnRepositoryException() throws Exception {
        mockVersionManagerChain();

        when(versionManager.getBaseVersion(anyString()))
                .thenThrow(new RepositoryException("repo problem"));

        BlackbirdServiceException ex = assertThrows(
                BlackbirdServiceException.class,
                () -> versioningService.synchronizeVersion("/content/source", "/content/target")
        );

        assertEquals("repo problem", ex.getMessage());
        verify(resolver, times(1)).close();
    }
}