package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.dto.ContentReference;

import java.util.List;

public interface ReferenceCollectorService {

    List<ContentReference> getReferences(String rootPath);
}
