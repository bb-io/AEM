package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.dto.BlackbirdPageEventSearchResult;
import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import io.blackbird.aemconnector.core.objects.PageEventSearchParams;
import org.apache.sling.api.resource.LoginException;

public interface BlackbirdPageEventService {

    BlackbirdPageEventSearchResult searchPageEvents(PageEventSearchParams params) throws LoginException, BlackbirdInternalErrorException;
}
