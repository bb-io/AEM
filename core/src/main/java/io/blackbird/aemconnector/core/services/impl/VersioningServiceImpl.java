package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.objects.VersionSyncResult;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.VersioningService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import java.util.Optional;

import static io.blackbird.aemconnector.core.utils.ObjectUtils.ensureNotNull;

@Slf4j
@Component(service = VersioningService.class, immediate = true)
public class VersioningServiceImpl implements VersioningService {

    @Reference
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Override
    public VersionSyncResult synchronizeVersion(final String sourcePath, final String targetPath) {
        try (final ResourceResolver resolver = serviceUserResolverProvider.getTranslationWriterResolver()) {
            final VersionManager versionManager = ensureNotNull(
                    getVersionManager(resolver),
                    () -> new BlackbirdServiceException("Failed to get VersionManager"));

            final String sourceBaseVersionName = getBaseVersionName(sourcePath, versionManager);
            final String targetBaseVersionName = getBaseVersionName(targetPath, versionManager);

            int numberOfVersionsDiff = getVersionCountDifference(sourcePath, targetPath, resolver);

            String targetCurrentVersionName = targetBaseVersionName;
            for (int i = 0; i < numberOfVersionsDiff; i++) {
                Version version = createVersion(targetPath, versionManager);
                targetCurrentVersionName = version.getName();
            }

            return VersionSyncResult.builder()
                    .sourceVersion(sourceBaseVersionName)
                    .targetOldVersion(targetBaseVersionName)
                    .targetNewVersion(targetCurrentVersionName)
                    .build();

        } catch (LoginException | RepositoryException e) {
            log.error(e.getMessage());
            throw new BlackbirdServiceException(e.getMessage());
        }
    }

    @Override
    public int getVersionCountDifference(final String sourcePath, final String targetPath) {
        try (final ResourceResolver resolver = serviceUserResolverProvider.getTranslationWriterResolver()) {
            return getVersionCountDifference(sourcePath, targetPath, resolver);
        } catch (LoginException | RepositoryException e) {
            log.error(e.getMessage());
            throw new BlackbirdServiceException(e.getMessage());
        }

    }

    private int getVersionCountDifference(final String sourcePath, final String targetPath, final ResourceResolver resolver) throws RepositoryException {
        final VersionManager versionManager = ensureNotNull(
                getVersionManager(resolver),
                () -> new BlackbirdServiceException("Failed to get VersionManager"));

        int sourceNumberOfVersions = getNumberOfVersions(sourcePath, versionManager);
        int targetNumberOfVersions = getNumberOfVersions(targetPath, versionManager);

        return sourceNumberOfVersions - targetNumberOfVersions;
    }

    private String getBaseVersionName(final String path, final VersionManager vm) throws RepositoryException {
        return vm.getBaseVersion(path).getName();
    }

    private Version createVersion(final String path, final VersionManager vm) throws RepositoryException {
        return vm.checkpoint(path);
    }

    private int getNumberOfVersions(final String path, final VersionManager vm) throws RepositoryException {
        final VersionIterator allVersions = vm.getVersionHistory(path).getAllVersions();
        int size = 0;
        while(allVersions.hasNext()) {
            allVersions.nextVersion();
            size++;
        }
        return size;
    }

    private VersionManager getVersionManager(final ResourceResolver resolver) throws RepositoryException {
        final Workspace workspace = Optional.ofNullable(resolver.adaptTo(Session.class))
                .map(Session::getWorkspace)
                .orElse(null);

        if (workspace == null) {
            return null;
        }

        return workspace.getVersionManager();
    }
}
