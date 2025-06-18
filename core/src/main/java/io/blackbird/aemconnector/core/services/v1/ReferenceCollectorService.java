package io.blackbird.aemconnector.core.services.v1;

import io.blackbird.aemconnector.core.dto.v1.ContentReference;

import java.util.List;

public interface ReferenceCollectorService {

    List<ContentReference> getReferences(String rootPath);
}
