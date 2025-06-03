package io.blackbird.aemconnector.core.services.v2;

import io.blackbird.aemconnector.core.dto.v2.ContentReference;
import org.apache.sling.api.resource.Resource;

import java.util.List;

public interface ReferenceCollectorService {

    List<ContentReference> getReferences(Resource resource);
}
