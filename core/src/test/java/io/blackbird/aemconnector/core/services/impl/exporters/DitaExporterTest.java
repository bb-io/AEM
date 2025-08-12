package io.blackbird.aemconnector.core.services.impl.exporters;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DitaExporterTest {

    private static final String SAMPLE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<topic id=\"sample\">" +
            "<title>Sample DITA</title>" +
            "<body><p>Test content</p></body>" +
            "</topic>";

    @Mock
    private Resource resource;

    @Mock
    private Resource binaryResource;

    private DitaExporter target;

    @BeforeEach
    void setUp() {
        target = new DitaExporter();
    }

    @Test
    void shouldBeAbleToExportDitaFile() {
        assertTrue(target.canExport(ContentType.DITA));
        assertFalse(target.canExport(ContentType.UNKNOWN));
        assertFalse(target.canExport(ContentType.CONTENT_FRAGMENT));
    }

    @Test
    void shouldSuccessfullyExportDitaFile() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(SAMPLE_XML.getBytes(StandardCharsets.UTF_8));
        when(resource.getChild("jcr:content/renditions/original")).thenReturn(binaryResource);
        when(binaryResource.adaptTo(InputStream.class)).thenReturn(inputStream);

        Serializable result = target.export(resource, new HashMap<>());

        assertNotNull(result);
        assertTrue(result instanceof ObjectNode);
        String jsonResult = result.toString();
        assertTrue(jsonResult.contains("Sample DITA"));
    }

    @Test
    void shouldThrowExceptionIfXmlBinaryFileIsAbsent() {
        when(resource.getChild("jcr:content/renditions/original")).thenReturn(null);

        BlackbirdServiceException exception = assertThrows(
                BlackbirdServiceException.class,
                () -> target.export(resource, new HashMap<>()));
        assertTrue(exception.getMessage().contains("Can not obtain dita file binary resource"));
    }

    @Test
    void shouldThrowExceptionIfXmlBinaryFileCanNotBeAdaptedToInputStream() {
        when(resource.getChild("jcr:content/renditions/original")).thenReturn(binaryResource);
        when(binaryResource.adaptTo(InputStream.class)).thenReturn(null);

        BlackbirdServiceException exception = assertThrows(
                BlackbirdServiceException.class,
                () -> target.export(resource, new HashMap<>()));
        assertTrue(exception.getMessage().contains("could not be adapted to InputStream"));
    }

    @Test
    void shouldThrowExceptionIfXmlIsIvalid() {
        String invalidXml = "Invalid XML";
        InputStream inputStream = new ByteArrayInputStream(invalidXml.getBytes(StandardCharsets.UTF_8));
        when(resource.getChild("jcr:content/renditions/original")).thenReturn(binaryResource);
        when(binaryResource.adaptTo(InputStream.class)).thenReturn(inputStream);

        assertThrows(
                BlackbirdServiceException.class,
                () -> target.export(resource, new HashMap<>()));
    }
}
