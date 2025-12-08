package io.blackbird.aemconnector.core.objects;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class VersionSyncResult {
    String sourceVersion;
    String targetOldVersion;
    String targetNewVersion;
}
