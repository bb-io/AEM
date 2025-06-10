package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;

import java.io.Serializable;
import java.util.Map;

public interface ContentExportService {
    Serializable exportContent(String path, ContentType contentType, Map<String, Object> options) throws BlackbirdServiceException;
}
