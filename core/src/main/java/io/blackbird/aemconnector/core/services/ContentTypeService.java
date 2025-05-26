package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;

public interface ContentTypeService {

    ContentType resolveContentType(String path) throws BlackbirdServiceException;
}
