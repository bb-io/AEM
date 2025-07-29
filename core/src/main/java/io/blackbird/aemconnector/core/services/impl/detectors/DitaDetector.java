package io.blackbird.aemconnector.core.services.impl.detectors;

import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.ContentTypeDetector;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(service = ContentTypeDetector.class)
public class DitaDetector implements ContentTypeDetector {

    private static final List<String> DITA_FILE_EXTENSIONS = Arrays.asList("xml", "dita", "ditamap", "ditaval");
    @Override
    public boolean detects(Resource resource) {
        final String path = resource.getPath();
        String extension = StringUtils.substringAfterLast(path, ".");
        return DITA_FILE_EXTENSIONS.contains(extension);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.DITA;
    }

    @Override
    public int getRank() {
        return 1;
    }
}
