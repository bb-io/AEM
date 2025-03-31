package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.dto.BlackbirdPageEventSearchResult;
import io.blackbird.aemconnector.core.models.BlackbirdEventViewerPage;
import io.blackbird.aemconnector.core.objects.PageEventSearchParams;

import java.util.List;

public interface BlackbirdPageEventService {

    BlackbirdPageEventSearchResult searchPageEvents(PageEventSearchParams params);
}
