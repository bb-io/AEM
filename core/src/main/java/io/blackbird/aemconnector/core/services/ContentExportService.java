package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;

import java.io.Serializable;

public interface ContentExportService {
    Serializable exportContent(String path, ContentType contentType) throws BlackbirdServiceException;
}
