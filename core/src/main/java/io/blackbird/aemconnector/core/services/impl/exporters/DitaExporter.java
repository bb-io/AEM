package io.blackbird.aemconnector.core.services.impl.exporters;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentExporter;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.utils.ObjectUtils;
import io.blackbird.aemconnector.core.utils.Xml2JsonUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component(service = ContentExporter.class)
public class DitaExporter implements ContentExporter {
    private static final String DITA_FILE_BINARY_PATH = "jcr:content/renditions/original";
    private static final String TYPE = "type";
    private static final String XML = "xml";

    @Override
    public boolean canExport(ContentType contentType) {
        return ContentType.DITA == contentType;
    }

    @Override
    public Serializable export(Resource resource, Map<String, Object> options) throws BlackbirdServiceException {
        Resource ditaFileBinaryResource = ObjectUtils.ensureNotNull(
                resource.getChild(DITA_FILE_BINARY_PATH),
                () -> new BlackbirdServiceException(String.format("Can not obtain dita file binary resource for %s",
                        resource.getPath())));
        InputStream ditaFileInputStream = ObjectUtils.ensureNotNull(
                ditaFileBinaryResource.adaptTo(InputStream.class),
                () -> new BlackbirdServiceException(String.format("Resource %s could not be adapted to InputStream",
                        ditaFileBinaryResource.getPath())));

        try {
            return XML.equals(options.get(TYPE))
                    ? IOUtils.toString(ditaFileInputStream, StandardCharsets.UTF_8)
                    : Xml2JsonUtil.convert(ditaFileInputStream);
        } catch (Exception e) {
            throw new BlackbirdServiceException(e.getMessage(), e);
        }
    }
}
