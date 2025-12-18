package io.blackbird.aemconnector.core.utils;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class PageInheritanceUtilsTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);
    private Node node;

    @BeforeEach
    void setUp() {
        Resource resource = context.create().resource("/content/test");
        node = resource.adaptTo(Node.class);
    }

    @Test
    void shouldReturnTrueWhenNodeIsLocal() throws RepositoryException {
        assertTrue(PageInheritanceUtils.isLocalNode(node));
    }

    @Test
    void shouldReturnFalseWhenNodeHasLiveRelationshipMixin() throws RepositoryException {
        Node node = mock(Node.class);
        when(node.isNodeType("cq:LiveRelationship")).thenReturn(true);

        assertFalse(PageInheritanceUtils.isLocalNode(node));
    }

    @Test
    void shouldReturnFalseWhenNodeHasMasterProperty() throws RepositoryException {
        node.setProperty("cq:master", "/content/test");
        assertFalse(PageInheritanceUtils.isLocalNode(node));
    }

    @Test
    void shouldReturnTrueWhenNodeHasLiveSyncCancelledMixin() throws RepositoryException {
        Node node = mock(Node.class);
        when(node.isNodeType("cq:LiveSyncCancelled")).thenReturn(true);

        assertTrue(PageInheritanceUtils.isNodeInheritanceCancelled(node));
    }

    @Test
    void shouldReturnTrueWhenNodeHasCancelledForChildrenProperty() throws RepositoryException {
        node.setProperty("cq:isCancelledForChildren", true);
        assertTrue(PageInheritanceUtils.isNodeInheritanceCancelled(node));
    }

    @Test
    void shouldReturnTrueWhenNodeHasPropertyLiveSyncCancelledMixinAndPropertyInCancelledList() throws RepositoryException {
        Node node = mock(Node.class);
        Property property = mock(Property.class);
        Value value = mock(Value.class);

        when(node.isNodeType("cq:PropertyLiveSyncCancelled")).thenReturn(true);
        when(property.getName()).thenReturn("jcr:title");
        when(value.getString()).thenReturn("jcr:title");
        when(node.hasProperty("cq:propertyInheritanceCancelled")).thenReturn(true);
        when(node.getProperty("cq:propertyInheritanceCancelled")).thenReturn(mock(Property.class));
        when(node.getProperty("cq:propertyInheritanceCancelled").getValues()).thenReturn(new Value[]{value});

        assertTrue(PageInheritanceUtils.isPropertyInheritanceCancelled(node, property));
    }

    @Test
    void shouldReturnFalseWhenNodeHasPropertyLiveSyncCancelledMixinAndPropertyNotInCancelledList() throws RepositoryException {
        Node node = mock(Node.class);
        Property property = mock(Property.class);
        Value value = mock(Value.class);
        Property cancelledProperty = mock(Property.class);

        when(node.isNodeType("cq:PropertyLiveSyncCancelled")).thenReturn(true);
        when(property.getName()).thenReturn("jcr:title");
        when(value.getString()).thenReturn("cq:description");
        when(cancelledProperty.getValues()).thenReturn(new Value[]{value});
        when(node.hasProperty("cq:propertyInheritanceCancelled")).thenReturn(true);
        when(node.getProperty("cq:propertyInheritanceCancelled")).thenReturn(cancelledProperty);

        assertFalse(PageInheritanceUtils.isPropertyInheritanceCancelled(node, property));
    }

    @Test
    void shouldExportPropertyWhenNodeIsLocal() throws RepositoryException {
        Property property = node.setProperty("jcr:title", "Test");
        assertTrue(PageInheritanceUtils.shouldExportProperty(node, property));
    }

    @Test
    void shouldExportPropertyWhenNodeInheritanceCancelled() throws RepositoryException {
        node.setProperty("cq:isCancelledForChildren", true);
        Property property = node.setProperty("jcr:title", "Test");
        assertTrue(PageInheritanceUtils.shouldExportProperty(node, property));
    }

    @Test
    void shouldExportPropertyWhenPropertyInheritanceCancelled() throws RepositoryException {
        Node node = mock(Node.class);
        Property property = mock(Property.class);
        Value value = mock(Value.class);
        Property cancelledProperty = mock(Property.class);

        when(node.isNodeType("cq:LiveRelationship")).thenReturn(true);
        when(node.isNodeType("cq:LiveSyncCancelled")).thenReturn(false);
        when(node.hasProperty("cq:isCancelledForChildren")).thenReturn(false);
        when(node.isNodeType("cq:PropertyLiveSyncCancelled")).thenReturn(true);
        when(value.getString()).thenReturn("jcr:title");
        when(cancelledProperty.getValues()).thenReturn(new Value[]{value});
        when(node.hasProperty("cq:propertyInheritanceCancelled")).thenReturn(true);
        when(node.getProperty("cq:propertyInheritanceCancelled")).thenReturn(cancelledProperty);
        when(property.getName()).thenReturn("jcr:title");

        assertTrue(PageInheritanceUtils.shouldExportProperty(node, property));
    }

    @Test
    void shouldNotExportPropertyWhenNodeNotLocalAndInheritanceNotCancelled() throws RepositoryException {
        Node node = mock(Node.class);
        Property property = mock(Property.class);

        when(node.isNodeType("cq:LiveRelationship")).thenReturn(true);
        when(node.isNodeType("cq:LiveSyncCancelled")).thenReturn(false);
        when(node.isNodeType("cq:PropertyLiveSyncCancelled")).thenReturn(false);

        assertFalse(PageInheritanceUtils.shouldExportProperty(node, property));
    }
}
