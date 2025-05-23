package io.blackbird.aemconnector.core.services;

import java.io.Serializable;

public interface ContentExportService {
    Serializable exportContent(String path, ContentType contentType);
}
