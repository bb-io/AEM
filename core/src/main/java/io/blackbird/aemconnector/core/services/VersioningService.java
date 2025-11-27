package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.objects.VersionSyncResult;

public interface VersioningService {
    VersionSyncResult synchronizeVersion(String sourcePath, String targetPath);

    int getVersionCountDifference(String sourcePath, String targetPath);
}
