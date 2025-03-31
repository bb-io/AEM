package io.blackbird.aemconnector.core.dto;

import io.blackbird.aemconnector.core.models.BlackbirdEventViewerPage;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class BlackbirdPageEventSearchResult {
    long totalMatches;
    boolean hasMore;
    int results;
    List<BlackbirdEventViewerPage> pages;
}
